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
package javaee.samples.frameworks.injection.spi;

import javaee.samples.frameworks.injection.jms.JMSContextMock;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.inject.Inject;
import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javaee.samples.frameworks.injection.spi.JMSResourceCtx.CTX;

public class JMSContextInjectionPoint implements InjectionPoint<Inject> {
    private static final Collection<String> JMS_CTX_MANAGED_METHODS_FAIL =
            asList("setClientID", "setExceptionListener", "start", "stop", "setAutoStart", "close", "commit",
                    "rollback", "recover");

    private final Deque<JMSContextMock> ctx = new ConcurrentLinkedDeque<>();

    @Override
    public Class<Inject> getAnnotationType() {
        return Inject.class;
    }

    @Override
    public <T> Optional<Object> lookupOf(Class<?> declaredInjectionType, Inject injectionAnnotation,
                                         T bean, Class<? extends T> beanType) {

        return declaredInjectionType == JMSContext.class ? of(jmsContext(bean)) : empty();
    }

    @Override
    public void destroy() {
        ctx.forEach(JMSContextMock::closeConnection);
    }

    private <T> JMSContextMock jmsContext(T bean) {
        JMSContextMock jmsCtx = MessageDrivenContext.MDCTX.get().get(bean);
        if (jmsCtx == null) {
            jmsCtx = CTX.startJMSCtx();
            ctx.offer(jmsCtx);
        }
        return jmsCtx;
    }

    /**
     * todo use it if transactional session
     */
    private static JMSContext proxy(JMSContext ctx) {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class<?>[]{JMSContext.class});
        Class<?> proxyClass = factory.createClass();
        try {
            Object proxy = proxyClass.newInstance();
            ((ProxyObject) proxy).setHandler((self, overridden, forwarder, args) -> {
                if (JMS_CTX_MANAGED_METHODS_FAIL.contains(overridden.getName()))
                    throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");

                return overridden.invoke(ctx, args);
            });
            return (JMSContext) proxy;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Could not create proxy " + proxyClass, e);
        }
    }
}
