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
package javaee.samples.frameworks.injection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.addAll;
import static javaee.samples.frameworks.injection.DB.H2;
import static javaee.samples.frameworks.injection.H2Storage.DEFAULT_STORAGE;
import static javaee.samples.frameworks.injection.H2Storage.DISABLE_MV_STORE;
import static javaee.samples.frameworks.injection.H2Storage.ENABLE_MVCC;
import static javaee.samples.frameworks.injection.H2Storage.ENABLE_MV_STORE;
import static javaee.samples.frameworks.injection.H2Storage.MULTI_THREADED_1;
import static javaee.samples.frameworks.injection.Mode.DB2;
import static javaee.samples.frameworks.injection.Mode.DEFAULT_MODE;
import static javaee.samples.frameworks.injection.Mode.DERBY;
import static javaee.samples.frameworks.injection.Mode.HSQLDB;
import static javaee.samples.frameworks.injection.Mode.MSSQL;
import static javaee.samples.frameworks.injection.Mode.MYSQL;
import static javaee.samples.frameworks.injection.Mode.ORACLE;
import static javaee.samples.frameworks.injection.Mode.PostgreSQL;

public final class JPARuleBuilder {
    private final Collection<Class<?>> preferableDomains = new ArrayList<>();
    private final Map<String, String> properties;
    private String unitName;
    private H2Storage storage;
    private Mode mode;
    private boolean useProperties = true, useAutoServerMode, closeSessionOnExitJVM;
    private int closeDbDelayInSeconds = -1;
    private DB db = H2;

    private JPARuleBuilder(String unitName) {
        properties = new HashMap<>();
        this.unitName = unitName;
        storage = DEFAULT_STORAGE;
        mode = DEFAULT_MODE;
    }

    public static JPARuleBuilder unitName(String unitName) {
        return new JPARuleBuilder(unitName);
    }

    public JPARuleBuilder database(DB database) {
        this.db = database;
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

    public JPARuleBuilder closeDbDelayInSeconds(int maxDelayToDie) {
        this.closeDbDelayInSeconds = maxDelayToDie;
        return this;
    }

    public JPARuleBuilder noInternalProperties() {
        useProperties = false;
        return this;
    }

    public JPARuleBuilder properties(Map<String, String> props) {
        if (props != null) {
            properties.putAll(props);
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

    Collection<Class<?>> getPreferableDomains() {
        return preferableDomains;
    }

    Map<String, String> getProperties() {
        return properties;
    }

    String getUnitName() {
        return unitName;
    }

    H2Storage getStorage() {
        return storage;
    }

    Mode getMode() {
        return mode;
    }

    boolean isUseProperties() {
        return useProperties;
    }

    boolean isUseAutoServerMode() {
        return useAutoServerMode;
    }

    boolean isCloseSessionOnExitJVM() {
        return closeSessionOnExitJVM;
    }

    int getCloseDbDelayInSeconds() {
        return closeDbDelayInSeconds;
    }

    DB getDb() {
        return db;
    }
}
