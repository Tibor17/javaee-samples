/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package timer;

import javax.enterprise.concurrent.ContextService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractExtensionOnStartupTimer<T extends Runnable> implements Extension {

    private volatile ScheduledFuture<?> future;

    protected abstract TimeUnit getTimeUnit();
    protected abstract long getPeriodTime();
    protected abstract Class<T> getJobType();

    public final void afterInitialized(@Observes @Initialized(ApplicationScoped.class) Object event)
            throws NamingException {

        ManagedScheduledExecutorService executor =
                lookupBean("java:comp/DefaultManagedScheduledExecutorService", ManagedScheduledExecutorService.class);

        ContextService proxy = lookupBean("java:comp/DefaultContextService", ContextService.class);

        future = executor.scheduleAtFixedRate(
                () -> proxy.createContextualProxy(lookupBean(getJobType()), Runnable.class).run(),
                0,
                getPeriodTime(),
                getTimeUnit());
    }

    public void afterDestroyed(@Observes @Destroyed(ApplicationScoped.class) Object event) {
        future.cancel(false);
    }

    private static <T> T lookupBean(Class<T> beanType) {
        BeanManager bm = CDI.current().getBeanManager();
        Set<Bean<?>> beans = bm.getBeans(beanType);
        Bean<?> bean = bm.resolve(beans);
        CreationalContext<?> ctx = bm.createCreationalContext(bean);
        return beanType.cast(bm.getReference(bean, beanType, ctx));
    }

    private static <T> T lookupBean(String jndi, Class<T> beanType) throws NamingException {
        InitialContext ctx = new InitialContext();
        return beanType.cast(ctx.lookup(jndi));
    }
}
