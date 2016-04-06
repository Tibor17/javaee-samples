

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

