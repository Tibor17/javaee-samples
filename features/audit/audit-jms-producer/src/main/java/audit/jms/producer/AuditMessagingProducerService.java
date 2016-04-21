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
package audit.jms.producer;

import audit.domain.Audit;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApplicationScoped
public class AuditMessagingProducerService {

    @Inject
    JMSContext ctx;

    @Resource(mappedName = "java:jms/topic/audit")
    Topic topic;

    public void send(@NotNull @Valid Audit audit) {
        ctx.createProducer()
                .send(topic, audit);
    }
}
//http://tomee.apache.org/examples-trunk/injection-of-connectionfactory/README.html
//http://stackoverflow.com/questions/18464499/jms-request-response-pattern-in-transactional-environment
///http://what-when-how.com/enterprise-javabeans-3-1/message-driven-beans-enterprise-javabeans-3-1/
//https://docs.jboss.org/jbossas/docs/Server_Configuration_Guide/4/html/JMS_Examples-A_Point_To_Point_With_MDB_Example.html
