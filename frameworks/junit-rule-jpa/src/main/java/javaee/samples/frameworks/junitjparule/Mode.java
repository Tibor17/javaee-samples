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
package javaee.samples.frameworks.junitjparule;

enum Mode {
    DEFAULT_MODE(null),
    ORACLE("Oracle"),
    MYSQL("MySQL"),
    DB2("DB2"),
    DERBY("Derby"),
    HSQLDB("HSQLDB"),
    MSSQL("MSSQLServer"),
    PostgreSQL("PostgreSQL");

    private final String mode;

    private Mode(String mode) {
        this.mode = mode;
    }

    public String getModeAsString() {
        return mode;
    }
}
