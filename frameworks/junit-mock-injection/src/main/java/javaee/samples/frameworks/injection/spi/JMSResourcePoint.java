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

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import javax.annotation.Resource;
import javax.jms.*;
import javax.jms.Queue;
import java.lang.IllegalStateException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javaee.samples.frameworks.injection.spi.JMSResourceCtx.CTX;

public class JMSResourcePoint implements InjectionPoint<Resource> {
    private static final Collection<String> JNDI_CF = asList("jms/ConnectionFactory", "java:/ConnectionFactory",
            "/ConnectionFactory", "java:comp/DefaultJMSConnectionFactory", "java:jboss/DefaultJMSConnectionFactory",
            "java:/JmsXA");

    @Override
    public Class<Resource> getAnnotationType() {
        return Resource.class;
    }

    @Override
    public <T> Optional<Object> lookupOf(Class<?> declaredInjectionType, Resource injectionAnnotation, T bean,
                                         Class<? extends T> beanType) {
        String mapping = injectionAnnotation.mappedName();
        if (mapping == null) mapping = injectionAnnotation.lookup();
        if (JNDI_CF.contains(mapping)) {
            CTX.startBrokerIfAbsent();
            return of(CTX.getConnectionFactory());
        } else if (declaredInjectionType == Queue.class) {
            Queue queue = CTX.getQueues().computeIfAbsent(mapping, ActiveMQQueue::new);
            sanityCheckResource(injectionAnnotation.type(), mapping, beanType);
            return of(queue);
        } else if (declaredInjectionType == Topic.class) {
            Topic topic = CTX.getTopics().computeIfAbsent(mapping, ActiveMQTopic::new);
            sanityCheckResource(injectionAnnotation.type(), mapping, beanType);
            return of(topic);
        }
        return empty();
    }

    @Override
    public void destroy() {
    }

    private static void sanityCheckResource(Class<?> injectionType, String mapping, Class<?> beanType) {
        if (injectionType != Object.class
                && (injectionType.isAssignableFrom(Queue.class) || injectionType.isAssignableFrom(Topic.class))) {
            throw new IllegalStateException("No javax.jms.Queue nor javax.jms.Topic found with JNDI mapped name \""
                    + mapping
                    + "\" in bean "
                    + beanType.getName());
        }
    }
}
