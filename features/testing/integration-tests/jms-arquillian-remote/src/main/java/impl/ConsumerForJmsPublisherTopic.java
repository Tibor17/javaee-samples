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
package impl;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * This does not work because of unnecessary jboss-web.xml which influenced the IT in wrong manner.
 * Due to using java:/-less JNDI name in destinationLookup use another example:
 * {@link QueueTestMDB} which has destinationLookup=jms/queue/test and and WF10 configuration
 * /subsystem=messaging-activemq/server=default/jms-queue=TestQ/:add(entries=["jms/queue/test java:/jboss/exported/jms/queue/TestQ"])
 */
/*@MessageDriven(activationConfig =  {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/topic/publisher"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        //@ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "NewsType = 'Sports' OR NewsType = 'Opinion'")
})*/
public class ConsumerForJmsPublisherTopic /*implements MessageListener, ExceptionListener*/ {
    private static final Logger LOG = Logger.getLogger(ConsumerForJmsPublisherTopic.class.getName());

    @Inject
    QueueTestStats stats;

    /*@Override
    public void onMessage(Message message) {
        try {
            stats.setText(message.getBody(String.class));
        } catch (JMSException e) {
            LOG.log(SEVERE, e, e::getLocalizedMessage);
        }
    }

    @Override
    public void onException(JMSException e) {
        LOG.log(SEVERE, e, e::getLocalizedMessage);
    }*/
}
