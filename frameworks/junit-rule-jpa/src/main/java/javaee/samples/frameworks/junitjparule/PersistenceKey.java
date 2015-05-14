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

import java.io.File;

import static java.util.Objects.requireNonNull;

final class PersistenceKey {
    private final String unitName;
    private final File databaseStorage;
    private final boolean transactional;

    PersistenceKey(String unitName, File databaseStorage, boolean transactional) {
        this.unitName = requireNonNull(unitName, "unitName is null");
        this.databaseStorage = requireNonNull(databaseStorage, "databaseStorage is null");
        this.transactional = transactional;
    }

    String getUnitName() {
        return unitName;
    }

    File getDatabaseStorage() {
        return databaseStorage;
    }

    boolean isTransactional() {
        return transactional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersistenceKey that = (PersistenceKey) o;

        return unitName.equals(that.unitName) && databaseStorage.equals(that.databaseStorage) && transactional == that.transactional;
    }

    @Override
    public int hashCode() {
        int result = unitName.hashCode();
        result = 31 * result + databaseStorage.hashCode();
        result = 31 * result + (transactional ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PersistenceKey{" +
                "unitName='" + unitName + '\'' +
                ", databaseStorage=" + databaseStorage +
                ", transactional=" + transactional +
                '}';
    }
}
