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

import javax.annotation.Resource;
import javax.enterprise.concurrent.ContextService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AfterInitialized implements Extension {

    /*
    @Resource
    ContextService proxy;
    */

    /*
    @Inject
    Job job;
    */

    /*
    @Resource//(lookup = "java:comp/DefaultManagedScheduledExecutorService")
    ManagedScheduledExecutorService executor;
    */

    volatile ScheduledFuture<?> future;

    public void afterInitialized(@Observes @Initialized(ApplicationScoped.class) Object event) throws NamingException {
        InitialContext ctx = new InitialContext();

        ManagedScheduledExecutorService executor =
                (ManagedScheduledExecutorService) ctx.lookup("java:comp/DefaultManagedScheduledExecutorService");

        ContextService proxy = (ContextService) ctx.lookup("java:comp/DefaultContextService");

        future = executor.scheduleAtFixedRate(() ->
        {
            Instance<Job> jobs = CDI.current().select(Job.class);
            Job job = jobs.get();
            try {
                proxy.createContextualProxy(job, Runnable.class).run();
            } finally {
                jobs.destroy(job);
            }
        }, 0, 1, SECONDS);
    }

    public void afterDestroyed(@Observes @Destroyed(ApplicationScoped.class) Object event) {
        future.cancel(false);
    }
}
