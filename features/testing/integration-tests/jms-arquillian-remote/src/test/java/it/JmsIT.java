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
package it;

import audit.domain.Audit;
import audit.domain.AuditFlow;
import audit.jms.producer.AuditMessagingProducerService;
import impl.CdiBean;
import impl.QueueTestStats;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import static java.lang.System.getProperty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.notNullValue;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * https://docs.jboss.org/arquillian/reference/1.0.0.Alpha1/en-US/html_single/
 * https://github.com/aslakknutsen/arquillian-showcase/tree/master/jms
 */
@RunWith(Arquillian.class)
public class JmsIT {
    private static final Logger LOG = Logger.getLogger(JmsIT.class.getName());

    @Resource(name = "java:/jms/queue/test")
    Queue testQueue;

    @Resource(mappedName = "java:/jboss/exported/jms/queue/TestQ")
    Queue testQ;

    @Resource(name = "jms/topic/publisher")
    Topic publisher;

    @Resource(lookup = "jms/PublisherTopic")
    Topic publisherByGlobalJndiName;

    @Inject
    JMSContext jmsContext;

    @Inject
    QueueTestStats stats;

    @Inject
    AuditMessagingProducerService producer;

    @Deployment
    @OverProtocol("Servlet 3.0")
    public static Archive<?> createTestArchive() {
        return create(MavenImporter.class)
                .loadPomFromFile("pom.xml")
                .importBuildOutput()
                .as(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new File(getProperty("web.xml")), "web.xml")
                .addAsWebInfResource(new File(getProperty("jboss-web.xml")), "jboss-web.xml");
    }

    @Before
    public void init() {
        stats.setText(null);
    }

    @Test
    @InSequence(1)
    public void test() throws InterruptedException, JMSException {
        jmsContext.createProducer()
                .send(testQueue, "%text%");

        assertThat(testQueue.getQueueName(), is(testQ.getQueueName()));
        assertThat(testQueue.getQueueName(), is("TestQ"));
        assertThat(jmsContext.createQueue("TestQ").getQueueName(), is("TestQ"));

        assertThat(stats.awaitText(), is("%text%"));
    }

    @Test
    @InSequence(2)
    public void test2() throws InterruptedException, JMSException {
        jmsContext.createProducer()
                .send(testQ, "%text%");

        assertThat(testQueue.getQueueName(), is(testQ.getQueueName()));
        assertThat(testQueue.getQueueName(), is("TestQ"));

        assertThat(stats.awaitText(), is("%text%"));
    }

    @Test
    public void publisherNameShouldBePublisherTopic() throws JMSException {
        assertThat(publisher.getTopicName(), is("PublisherTopic"));
        assertThat(publisherByGlobalJndiName.getTopicName(), is("PublisherTopic"));

        /*jmsContext.createProducer()
                .send(publisher, "some text");

        assertThat(stats.awaitText(), is("some text"));*/
    }

    @Test
    public void auditConsumerShouldReceiveMessageFromProducer() {
        Audit origin = new Audit();
        UUID request = randomUUID();
        origin.setRequest(request);
        origin.setInitiator(5);
        origin.setModule("test");
        origin.setOperationKey("login");
        origin.setDescription("desc");
        origin.getFlows()
                .add(new AuditFlow());

        producer.send(origin);

        assertThat(stats.awaitText(), is(request.toString()));
    }

    /**
     * See the chapter "So, what’s next?" in tutorial
     * http://arquillian.org/blog/tags/examples/
     */
    @Test
    public void shouldInjectCdiBean(CdiBean bean) {
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getX(), is("x"));
    }
}
