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
package javaee.samples.frameworks.junitjparule.transactional.injection.constructor;

import javaee.samples.frameworks.junitjparule.entities.MyEntity;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.SUPPORTS;

public class SupportsOuterService {
    private @Inject
    InnerService service;

    @Transactional(SUPPORTS)
    public void saveOuter(String name) {
        MyEntity e = new MyEntity();
        e.setName(name + "-outer");
        service.saveInner(e);
    }
}
