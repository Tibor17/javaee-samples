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

import audit.domain.Audit;
import audit.domain.AuditFlow;
import audit.domain.AuditObjects;
import audit.query.search.api.AuditQuery;
import javaee.samples.frameworks.injection.InjectionRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.jms.*;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import static audit.query.search.api.SortField.SORT_BY_DATE;
import static audit.util.Dates.toXMLGregorianCalendar;
import static java.util.Calendar.SECOND;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.core.Is.is;

@Vetoed
@RunWith(InjectionRunner.class)
public class AuditQueryConsumerServiceTest {
    private final AuditQuery query = newQuery();
    private final Audit audit = newAudit();
    private final CountDownLatch synchronizer = new CountDownLatch(1);

    @Rule
    public final ErrorCollector errors = new ErrorCollector();

    @Inject
    JMSContext ctx;

    @Resource(mappedName = "java:jms/topic/auditquery/request")
    Topic requestQueue;

    @Inject
    AuditQueryConsumerService service;

    @Produces
    AuditSelector selector = q -> {
        errors.checkThat(q, is(query));
        return new AuditObjects(audit);
    };

    @Test
    public void shouldReplyAuditAfterQueryRequest() throws Exception {
        ObjectMessage msg = ctx.createObjectMessage(query);
        TemporaryQueue replyQueue = ctx.createTemporaryQueue();
        msg.setJMSReplyTo(replyQueue);

        ctx.createProducer()
                .send(requestQueue, msg);

        ctx.createConsumer(replyQueue)
                .setMessageListener(m -> {
                    try {
                        if (m instanceof ObjectMessage) {
                            ObjectMessage replyMessage = (ObjectMessage) m;
                            Object replied = replyMessage.getObject();

                            assertThat(replied)
                                    .isInstanceOf(AuditObjects.class);

                            AuditObjects auditObjects = (AuditObjects) replied;

                            assertThat(auditObjects)
                                    .extracting(AuditObjects::toList)
                                    .hasSize(1);

                            assertThat(auditObjects.toList().get(0).getStoredAt().getTime())
                                    .isEqualTo(audit.getStoredAt().getTime());

                            assertThat(auditObjects.toList().get(0).getStoredAt().getTimeZone())
                                    .isEqualTo(audit.getStoredAt().getTimeZone());

                            synchronizer.countDown();
                        } else {
                            fail("illegal message type");
                        }
                    } catch (JMSException e) {
                        errors.addError(e);
                        throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
                    } catch (Throwable t) {
                        errors.addError(t);
                        fail(t.getLocalizedMessage(), t);
                    }
                });

        assertThat(synchronizer.await(3, SECONDS))
                .describedAs("test timeout")
                .isTrue();
    }

    private static AuditQuery newQuery() {
        AuditQuery query = new AuditQuery();
        query.setOperationKey("operation");
        query.setDescription("desc");
        query.setInitiator(5L);
        query.setModule("mod");
        query.setError("err");
        query.setSortField(SORT_BY_DATE);
        query.setStartRowNum(10);
        query.setPageSize(20);
        query.setFrom(toXMLGregorianCalendar(Calendar.getInstance()));
        Calendar to = Calendar.getInstance();
        to.add(SECOND, 30);
        query.setTo(toXMLGregorianCalendar(to));
        return query;
    }

    private static Audit newAudit() {
        Audit original = new Audit();
        original.setRequest(randomUUID());
        original.setInitiator(5);
        original.setModule("test");
        original.setOperationKey("login");
        original.setDescription("desc");
        original.getFlows().add(new AuditFlow());
        return original;
    }
}
