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
package jms.wrappers;

import org.apache.activemq.broker.BrokerService;

import java.net.URI;

public final class JMSBroker {
    private final BrokerService broker;

    public JMSBroker(int port, boolean persistent) throws Exception {
        this(persistent);
        broker.addConnector("tcp://localhost:" + port);
    }

    public JMSBroker(URI connector, boolean persistent) throws Exception {
        this(persistent);
        broker.addConnector(connector);
    }

    private JMSBroker(boolean persistent) {
        broker = new BrokerService();
        if (persistent) {
            broker.setDeleteAllMessagesOnStartup(true);
        }
        broker.setPersistent(persistent);
        broker.setUseJmx(false);
    }

    public void start() throws Exception {
        broker.start();
    }

    public void stop() throws Exception {
        broker.stop();
    }
}
