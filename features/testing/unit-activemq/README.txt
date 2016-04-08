

                connection.setClientID(getName());
                session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                MessageConsumer consumer = session.createDurableSubscriber(destination, getName());
                consumer.setMessageListener(this);
                connection.start();



            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);
            ///producer.setTimeToLive(0);
            connection.start();

            message.setIntProperty("MsgNumber", id);

http://blog.teamlazerbeez.com/2010/10/08/using-hornetq-without-a-separate-jndi-server/
https://developer.jboss.org/thread/240356?start=0&tstart=0


https://developer.jboss.org/wiki/AS710FinalReleaseNotes
http://www.onjava.com/pub/a/onjava/excerpt/jms_ch2/index.html?page=4
http://activemq.apache.org/jndi-support.html
https://docs.jboss.org/hornetq/2.2.5.Final/user-manual/en/html/using-jms.html#d0e1227
https://docs.oracle.com/javaee/6/tutorial/doc/bnceh.html#bncen
https://docs.oracle.com/javaee/6/tutorial/doc/bncgl.html
http://tomee.apache.org/examples-trunk/simple-mdb/README.html
http://tomee.apache.org/examples-trunk/injection-of-connectionfactory/README.html
https://github.com/apache/tomee/blob/042d4d9fc647c32ee31c4c7455a4769817564340/examples/simple-mdb/src/test/java/org/superbiz/mdb/ChatBeanTest.java
http://docs.jboss.org/arquillian/reference/1.0.0.Alpha1/en-US/html_single/#examples.ejb
https://jaxenter.com/tutorial-arquillian-makes-testing-a-breeze-104518.html
https://developer.jboss.org/thread/162395?tstart=0
https://docs.jboss.org/weld/reference/latest/en-US/html/ri-spi.html#_injection_services
https://docs.jboss.org/ejb3/embedded/embedded.html
https://developer.jboss.org/wiki/EJB31Embeddable
https://developer.jboss.org/wiki/WildFlyElytron-ProjectSummary
https://www.youtube.com/watch?v=YYmMInxgNUs
http://www.tomitribe.com/blog/2014/06/apache-tomee-jax-rs-and-arquillian-starter-project/
