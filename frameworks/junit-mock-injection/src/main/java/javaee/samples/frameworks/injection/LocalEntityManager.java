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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * To prevent from memory leak, {@link LocalEntityManager} must be singleton.
 */
final class LocalEntityManager extends ThreadLocal<EntityManager> {

    public EntityManager get(EntityManagerFactory emf, Map<String, String> emProps) {
        EntityManager em = super.get();
        if (em == null) {
            em = emf.createEntityManager(emProps);
            set(em);
        } else if (!em.isOpen()) {
            remove();
            em = emf.createEntityManager(emProps);
            set(em);
        }
        return em;
    }
}
