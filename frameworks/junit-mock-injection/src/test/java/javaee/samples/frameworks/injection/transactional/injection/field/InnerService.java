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
package javaee.samples.frameworks.injection.transactional.injection.field;

import javaee.samples.frameworks.injection.entities.MyEntity;
import javaee.samples.frameworks.injection.dao.DAO;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class InnerService {
    @Inject
    private DAO dao;

    @Transactional
    public void saveInner(MyEntity e) {
        e.setName(e.getName() + "-inner");
        dao.persist(e);
    }
}
