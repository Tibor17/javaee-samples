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

import org.junit.runner.Description;

import java.util.Map;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

final class UnitPerClass {
    static UnitPerClass unitPerClass(String unit, Description d, Map<String, String> properties) {
        return new UnitPerClass(unit, requireNonNull(d).getClassName(), properties);
    }

    private final String unit, clazz;
    private final int propertiesHash;

    UnitPerClass(String unit, String clazz, Map<String, String> properties) {
        this.unit = requireNonNull(unit);
        this.clazz = requireNonNull(clazz);
        propertiesHash = new TreeMap<>(properties).hashCode();
    }

    String getUnit() {
        return unit;
    }

    String getClazz() {
        return clazz;
    }

    int getPropertiesHash() {
        return propertiesHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitPerClass that = (UnitPerClass) o;
        return propertiesHash == that.propertiesHash && unit.equals(that.unit) && clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
        int result = unit.hashCode();
        result = 31 * result + clazz.hashCode();
        result = 31 * result + propertiesHash;
        return result;
    }
}
