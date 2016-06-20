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
package audit.jms.reply;

import audit.domain.Audit;
import audit.domain.AuditObjects;
import audit.query.search.api.AuditQuery;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.util.Collections.emptySet;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class AuditQueryRequestAuditReplyService {
    private static final Logger LOG = getLogger(AuditQueryRequestAuditReplyService.class);

    @Inject
    JMSContext ctx;

    @Resource(mappedName = "java:jms/topic/auditquery/request")
    Topic requestTopic;

    public void queryAuditAsync(AuditQuery auditQuery, BiConsumer<Iterable<Audit>, JMSException> asyncConsumer)
            throws JMSException {
        TemporaryQueue replyQueue = request(auditQuery);
        ctx.createConsumer(replyQueue)
                .setMessageListener(msg -> {
                    try {
                        AuditObjects response = msg.getBody(AuditObjects.class);
                        asyncConsumer.accept(response == null ? emptySet() : response.toList(), null);
                    } catch (JMSException e) {
                        LOG.error(e.getLocalizedMessage());
                        asyncConsumer.accept(emptySet(), e);
                    }
                });
    }

    public Iterable<Audit> queryAuditWithNoWait(AuditQuery auditQuery) throws JMSException {
        TemporaryQueue replyQueue = request(auditQuery);
        AuditObjects om = ctx.createConsumer(replyQueue)
                .receiveBodyNoWait(AuditObjects.class);

        return om == null ? emptySet() : om.toList();
    }

    public Iterable<Audit> queryAudit(AuditQuery auditQuery, TimeUnit timeUnit, long timeout) throws JMSException {
        TemporaryQueue replyQueue = request(auditQuery);
        AuditObjects obs = ctx.createConsumer(replyQueue)
                .receiveBody(AuditObjects.class, timeUnit.toMillis(timeout));

        return obs == null ? emptySet() : obs.toList();
    }

    private TemporaryQueue request(AuditQuery auditQuery) throws JMSException {
        ObjectMessage request = ctx.createObjectMessage(auditQuery);
        TemporaryQueue replyQueue = ctx.createTemporaryQueue();
        request.setJMSReplyTo(replyQueue);

        ctx.createProducer()
                .send(requestTopic, request);

        return replyQueue;
    }
}
