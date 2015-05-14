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

import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;

import java.util.HashMap;
import java.util.Map;

import static javaee.samples.frameworks.junitjparule.H2Storage.*;
import static javaee.samples.frameworks.junitjparule.Mode.*;

public final class JPARuleBuilder {
    final Map<String, String> properties;
    String unitName;
    boolean transactional;
    boolean doNotCommitOwnTransaction;
    boolean joinTransaction;
    H2Storage storage;
    Mode mode;

    private JPARuleBuilder(String unitName) {
        properties = new HashMap<>();
        this.unitName = unitName;
        storage = DEFAULT_STORAGE;
        mode = DEFAULT_MODE;
    }

    public static JPARuleBuilder unitName(String unitName) {
        return new JPARuleBuilder(unitName);
    }

    public JPARuleBuilder properties(Map<String, String> properties) {
        if (properties != null) {
            this.properties.putAll(properties);
        }
        return this;
    }

    public JPARuleBuilder transactional() {
        transactional = true;
        return this;
    }

    /**
     * (Optional) Attempts to join transaction via {@link EntityManager#joinTransaction()} on own {@link EntityManager}.
     * The exception {@link TransactionRequiredException} is consumed and new transaction is created by the
     * entity manager itself if {@link #transactional()} is enabled while calling a transactional method.
     */
    public JPARuleBuilder joinTransaction() {
        joinTransaction = true;
        return this;
    }

    public JPARuleBuilder doNotCommitOwnTransaction() {
        doNotCommitOwnTransaction = true;
        return this;
    }

    public JPARuleBuilder storageDefault() {
        storage = DEFAULT_STORAGE;
        return this;
    }

    public JPARuleBuilder storageMVEnabled() {
        storage = ENABLE_MV_STORE;
        return this;
    }

    public JPARuleBuilder storageMVDisabled() {
        storage = DISABLE_MV_STORE;
        return this;
    }

    public JPARuleBuilder storageMVCC() {
        storage = ENABLE_MVCC;
        return this;
    }

    public JPARuleBuilder storageMultithreaded() {
        storage = MULTI_THREADED_1;
        return this;
    }

    public JPARuleBuilder modeDefault() {
        mode = DEFAULT_MODE;
        return this;
    }

    public JPARuleBuilder modeOracle() {
        mode = ORACLE;
        return this;
    }

    public JPARuleBuilder modeMySQL() {
        mode = MYSQL;
        return this;
    }

    public JPARuleBuilder modeDB2() {
        mode = DB2;
        return this;
    }

    public JPARuleBuilder modeDerby() {
        mode = DERBY;
        return this;
    }

    public JPARuleBuilder modeHSQLDB() {
        mode = HSQLDB;
        return this;
    }

    public JPARuleBuilder modeMSSQLServer() {
        mode = MSSQL;
        return this;
    }

    public JPARuleBuilder modePostgreSQL() {
        mode = PostgreSQL;
        return this;
    }

    public JPARule build() {
        return new JPARule(this);
    }
}
