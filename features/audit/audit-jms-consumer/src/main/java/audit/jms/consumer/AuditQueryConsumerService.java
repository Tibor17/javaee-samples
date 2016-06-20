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
package audit.jms.consumer;

import audit.domain.AuditObjects;
import audit.query.search.api.AuditQuery;
import org.slf4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

import static org.slf4j.LoggerFactory.getLogger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:jms/topic/auditquery/request"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class AuditQueryConsumerService implements MessageListener {
    private static final Logger LOG = getLogger(AuditQueryConsumerService.class);

    @Inject JMSContext ctx;
    @Inject AuditSelector selector;

    @Override
    public void onMessage(Message message) {
        try {
            AuditQuery query = message.getBody(AuditQuery.class);
            AuditObjects audits = selector.onMessage(query);

            message.setJMSCorrelationID(message.getJMSMessageID());
            Destination replyDestination = message.getJMSReplyTo();

            ctx.createProducer()
                    .send(replyDestination, audits);
        } catch (JMSException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
