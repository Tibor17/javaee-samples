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
import javax.ejb.LocalBean;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.logging.Logger;

@LocalBean
@MessageDriven(name = "MyMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/queue/test"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto_acknowledge")})
public class QueueTestMDB implements MessageListener {

    private final static Logger LOG = Logger.getLogger(QueueTestMDB.class.getName());

    @Inject
    QueueTestStats stats;

    @Override
    public void onMessage(Message message) {
        try {
            String text = message.getBody(String.class);
            stats.setText(text);
            LOG.info(text);
        } catch (JMSException e) {
            LOG.severe(e.getLocalizedMessage());
        }
    }
}
