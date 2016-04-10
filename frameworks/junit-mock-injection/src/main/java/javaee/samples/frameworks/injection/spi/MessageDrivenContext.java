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

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static javaee.samples.frameworks.injection.spi.JMSResourceCtx.CTX;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static javax.jms.Session.DUPS_OK_ACKNOWLEDGE;

public class MessageDrivenContext implements ContextInjector {
    private final Map<Object, Void> messageDrivenBeans = new WeakHashMap<>();
    private final Collection<Connection> connections = new ArrayList<>();

    @Override
    public <T> T bindContext(T bean, Class<?> beanType) {
        if (beanType.isAnnotationPresent(MessageDriven.class)) {
            if (!messageDrivenBeans.containsKey(bean)) {
                messageDrivenBean(bean, beanType);
                messageDrivenBeans.put(bean, null);
            }
        }
        return bean;
    }

    @Override
    public void destroy() {
        connections.forEach(MessageDrivenContext::close);
    }

    private <T> void messageDrivenBean(T bean, Class<? extends T> beanType) {
        if (MessageListener.class.isAssignableFrom(beanType)) {
            MessageDriven md = beanType.getAnnotation(MessageDriven.class);
            Collection<ActivationConfigProperty> properties = asList(md.activationConfig());
            ActiveMQDestination destination = lookupDestination(properties);
            ConnectionFactory factory = CTX.startConnectionFactory();
            try {
                Connection connection = factory.createConnection();
                connection.setClientID(lookupClientId(properties, destination.getPhysicalName()));
                Session session = connection.createSession(false, lookupAcknowledgeMode(properties));
                MessageConsumer consumer = createConsumer(properties, session, destination);
                consumer.setMessageListener((MessageListener) bean);
                connection.start();
                connections.add(connection);
            } catch (JMSException e) {
                throw new EJBException(e.getLocalizedMessage(), e);
            }
        } else {
            throw new EJBException("The message driven bean \""
                    + beanType.getName()
                    + "\" must implement the appropriate message listener interface \""
                    + MessageDriven.class.getName()
                    + "\".");
        }
    }

    private static MessageConsumer createConsumer(Collection<ActivationConfigProperty> properties,
                                                  Session session, Destination destination) throws JMSException {
        if (destination instanceof Topic) {
            boolean isDurable = hasTopicSubscriptionDurability(properties);
            if (isDurable) {
                String subscriptionName = lookupDurableTopicSubscriptionName(properties);
                // yet ActiveMQ 5.13.2 is JMS 1.1
                return session.createDurableSubscriber((Topic) destination, subscriptionName);
            }
        }
        return session.createConsumer(destination);
    }

    private static String lookupClientId(Collection<ActivationConfigProperty> properties, String fallback) {
        Optional<String> clientId =
                properties.stream()
                        .filter(p -> "clientId".equals(p.propertyName()))
                        .findFirst()
                        .map(ActivationConfigProperty::propertyValue);
        return clientId.orElse(fallback);
    }

    private static int lookupAcknowledgeMode(Collection<ActivationConfigProperty> properties) {
        Optional<Integer> acknowledgeMode =
                properties.stream()
                        .filter(p -> "acknowledgeMode".equals(p.propertyName()))
                        .findFirst()
                        .map(p -> p.propertyValue().equals("Dups_ok_acknowledge") ? DUPS_OK_ACKNOWLEDGE : AUTO_ACKNOWLEDGE);
        return acknowledgeMode.orElse(AUTO_ACKNOWLEDGE);
    }

    private static ActiveMQDestination lookupDestination(Collection<ActivationConfigProperty> properties) {
        Optional<ActivationConfigProperty> destinationType =
                properties.stream()
                        .filter(p -> "destinationType".equals(p.propertyName()))
                        .findFirst();

        String type = destinationType.orElseThrow(InvalidDestinationTypeException::new)
                .propertyValue();

        Optional<ActivationConfigProperty> destinationLookup =
                properties.stream()
                        .filter(p -> "destinationLookup".equals(p.propertyName()))
                        .findFirst();

        String jndi = destinationLookup.orElseThrow(InvalidDestinationLookupException::new)
                .propertyValue();

        return of(type)
                .map(destType -> toDestination(destType, jndi))
                .orElseThrow(InvalidDestinationTypeException::new);
    }

    private static boolean hasTopicSubscriptionDurability(Collection<ActivationConfigProperty> properties) {
        Optional<ActivationConfigProperty> property = properties.stream()
                .filter(p -> "subscriptionDurability".equals(p.propertyName()))
                .findFirst();

        if (!property.isPresent())
            return false;

        return property.map(ActivationConfigProperty::propertyValue)
                .map(v -> "Durable".equals(v) ? TRUE : ("NonDurable".equals(v) ? FALSE : null))
                .orElseThrow(InvalidDestinationLookupException::new);
    }

    private static String lookupDurableTopicSubscriptionName(Collection<ActivationConfigProperty> properties) {
        return properties.stream()
                .filter(p -> "subscriptionName".equals(p.propertyName()))
                .findFirst()
                .map(ActivationConfigProperty::propertyValue)
                .orElseThrow(InvalidDestinationLookupException::new);
    }

    private static ActiveMQDestination toDestination(String type, String jndi) {
        if (type.equals(Queue.class.getName())) return new ActiveMQQueue(jndi);
        else if (type.equals(Topic.class.getName())) return new ActiveMQTopic(jndi);
        return null;
    }

    private static void close(Connection connection) {
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
