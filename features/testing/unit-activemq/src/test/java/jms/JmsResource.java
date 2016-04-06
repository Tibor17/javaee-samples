package jms;

import javaee.samples.frameworks.junitjparule.spi.InjectionPoint;
import jms.wrappers.JMSBroker;
import jms.wrappers.JMSConsumer;
import jms.wrappers.JMSProducer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static jms.wrappers.JMSConsumer.createConsumerOnQueue;
import static jms.wrappers.JMSProducer.createProducerOnQueue;

public class JmsResource implements InjectionPoint<Resource> {
    private static final String SOCKET = System.getProperty("jms.broker.socket", "tcp://localhost:61616");

    private ConnectionFactory connectionFactory;
    private JMSConsumer consumer;
    private JMSProducer<Queue> producer;
    private JMSBroker broker;

    public JmsResource() throws Exception {
        broker = new JMSBroker(new URI(SOCKET), true);
        broker.start();
        connectionFactory = new ActiveMQConnectionFactory(SOCKET + "?jms.redeliveryPolicy.maximumRedeliveries=1&jms.redeliveryPolicy.initialRedeliveryDelay=0");
        consumer = createConsumerOnQueue(connectionFactory, "jms/queue/test", empty());
        producer = createProducerOnQueue(connectionFactory, "jms/queue/test");
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Resource.class;
    }

    @Override
    public <T> Optional<Object> lookupOf(Class<?> declaredInjectionType, Resource injectionAnnotation, T bean, Class<? extends T> beanType) {
        String mapping = injectionAnnotation.mappedName();
        if (mapping == null) mapping = injectionAnnotation.lookup();
        switch (mapping) {
            case "jms/ConnectionFactory": // Java EE spec
            case "java:/ConnectionFactory": // Java EE spec
            case "/ConnectionFactory": // JBoss
            case "java:comp/DefaultJMSConnectionFactory": // JBoss
            case "java:jboss/DefaultJMSConnectionFactory": // JBoss
            case "java:/JmsXA":
                return of(connectionFactory);

            case "java:jms/queue/test": // Java EE spec
            case "jms/queue/test": // Java EE spec
            case "queue/test": // JBoss
            case "/jms/queue/test": // JBoss
            case "/queue/test": // JBoss
                sanityCheckQueue(injectionAnnotation.type(), mapping, beanType);
                if (beanType.isAnnotationPresent(MessageDriven.class)) {
                    if (MessageListener.class.isAssignableFrom(beanType)) {
                        try {
                            consumer.getMessageConsumer().setMessageListener((MessageListener) bean);
                        } catch (JMSException e) {
                            throw new IllegalStateException(e.getLocalizedMessage(), e);
                        }
                    } else {
                        throw new EJBException("The message driven bean \""
                                + beanType.getName()
                                + "\" must implement the appropriate message listener interface \""
                                + MessageDriven.class.getName()
                                + "\".");
                    }
                }
                return of(producer.getDestination());

            default:
                return empty();
        }
    }

    @Override
    public void destroy() {
        try {
            consumer.getMessageConsumer().close();
            consumer.getConnection().close();
            producer.getMessageProducer().close();
            producer.getConnection().close();
            broker.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    private static void sanityCheckQueue(Class<?> injectionType, String mapping, Class<?> beanType) {
        if (injectionType != Object.class && injectionType.isAssignableFrom(Queue.class)) {
            throw new IllegalStateException("No javax.jms.Queue found with JNDI mapped name \""
                    + mapping
                    + "\" in bean "
                    + beanType.getName());
        }
    }
}
