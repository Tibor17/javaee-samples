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
package audit.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.jms.JMSContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static javaee.samples.frameworks.injection.spi.JMSResourceCtx.CTX;
import static javax.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
@SuppressWarnings("unused")
public class Producers {
    @Produces
    @ApplicationScoped
    public EntityManagerFactory create() {
        return createEntityManagerFactory("audit-jpa");
    }

    @Produces
    @RequestScoped
    public EntityManager produceEntityManager(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Produces
    public JMSContext produceJMSContext() {
        CTX.startupJMSCtx();
        return CTX.getJmsContext();
    }

    public void close(@Disposes EntityManager em) {
        em.close();
    }

    public void close(@Disposes EntityManagerFactory emf) {
        emf.close();
    }
}
