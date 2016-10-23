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

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import java.util.Optional;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public abstract class BaseJMS {
    private final ConnectionFactory connectionFactory;
    private final Connection connection;
    private final Session session;

    public BaseJMS(ConnectionFactory connectionFactory, Optional<String> connectionId) throws JMSException {
        this.connectionFactory = connectionFactory;
        connection = connectionFactory.createConnection();
        if (connectionId.isPresent()) {
            connection.setClientID(connectionId.get());
        }
        session = connection.createSession(false, AUTO_ACKNOWLEDGE);

    }

    public BaseJMS(String uri, Optional<String> connectionId) throws JMSException {
        this(new ActiveMQConnectionFactory(uri), connectionId);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }
}
