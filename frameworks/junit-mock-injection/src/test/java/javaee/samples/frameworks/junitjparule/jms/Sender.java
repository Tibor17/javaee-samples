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
package javaee.samples.frameworks.junitjparule.jms;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import static org.assertj.core.api.Assertions.assertThat;

public class Sender {

    @Inject
    JMSContext ctx;

    @Resource(mappedName = "java:jms/queue/test")
    Queue queue;

    public void send(String msg) {
        assertThat(ctx)
                .isNotNull();

        assertThat(queue)
                .isNotNull();

        ctx.createProducer()
                .send(queue, msg);
    }
}
