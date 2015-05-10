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

import static javaee.samples.frameworks.junitjparule.Mode.*;
import static javaee.samples.frameworks.junitjparule.H2Storage.*;

public class JUnitJpaRule {
    private final String unitName;
    private volatile boolean transactional;
    private volatile H2Storage storage;
    private volatile Mode mode;

    private JUnitJpaRule(String unitName) {
        this.unitName = unitName;
    }

    private JUnitJpaRule() {
        throw new IllegalStateException("Non instantiable.");
    }

    public static JUnitJpaRule unitName(String unitName) {
        return new JUnitJpaRule(unitName);
    }

    public JUnitJpaRule useTransactional() {
        transactional = true;
        return this;
    }

    public JUnitJpaRule storageDefault() {
        storage = DEFAULT_STORAGE;
        return this;
    }

    public JUnitJpaRule storageMVEnabled() {
        storage = ENABLE_MV_STORE;
        return this;
    }

    public JUnitJpaRule storageMVDisabled() {
        storage = DISABLE_MV_STORE;
        return this;
    }

    public JUnitJpaRule storageMVCC() {
        storage = ENABLE_MVCC;
        return this;
    }

    public JUnitJpaRule storageMultithreaded() {
        storage = MULTI_THREADED_1;
        return this;
    }

    public JUnitJpaRule modeDefault() {
        mode = DEFAULT_MODE;
        return this;
    }

    public JUnitJpaRule modeOracle() {
        mode = ORACLE;
        return this;
    }
}
