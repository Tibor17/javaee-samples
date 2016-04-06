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

import java.util.*;

import static java.util.Collections.addAll;
import static javaee.samples.frameworks.junitjparule.DB.H2;
import static javaee.samples.frameworks.junitjparule.H2Storage.*;
import static javaee.samples.frameworks.junitjparule.Mode.*;

public final class JPARuleBuilder {
    final Collection<Class<?>> preferableDomains = new ArrayList<>();
    final Map<String, String> properties;
    String unitName;
    H2Storage storage;
    Mode mode;
    boolean useProperties = true, useAutoServerMode, closeSessionOnExitJVM;
    int closeDbDelayInSeconds = -1;
    DB db = H2;

    private JPARuleBuilder(String unitName) {
        properties = new HashMap<>();
        this.unitName = unitName;
        storage = DEFAULT_STORAGE;
        mode = DEFAULT_MODE;
    }

    public static JPARuleBuilder unitName(String unitName) {
        return new JPARuleBuilder(unitName);
    }

    public JPARuleBuilder database(DB db) {
        this.db = db;
        return this;
    }

    public JPARuleBuilder preferableDomain(Class<?>... domains) {
        addAll(preferableDomains, domains);
        return this;
    }

    public JPARuleBuilder useAutoServerMode() {
        useAutoServerMode = true;
        return this;
    }

    public JPARuleBuilder closeSessionOnExitJVM() {
        closeSessionOnExitJVM = true;
        return this;
    }

    public JPARuleBuilder closeDbDelayInSeconds(int closeDbDelayInSeconds) {
        this.closeDbDelayInSeconds = closeDbDelayInSeconds;
        return this;
    }

    public JPARuleBuilder noInternalProperties() {
        useProperties = false;
        return this;
    }

    public JPARuleBuilder properties(Map<String, String> properties) {
        if (properties != null) {
            this.properties.putAll(properties);
        }
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
