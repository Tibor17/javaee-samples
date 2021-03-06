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
package javaee.samples.frameworks.tsupporth2;

import org.h2.tools.Server;

import java.sql.SQLException;

public enum H2Server {
    TCP;

    private final Server server;

    H2Server() {
        try {
            server = Server.createTcpServer("-tcpAllowOthers");
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        server.stop();
                    } finally {
                        server.shutdown();
                    }
                }
            });
        } catch (SQLException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    public Server getServer() {
        return server;
    }
}
