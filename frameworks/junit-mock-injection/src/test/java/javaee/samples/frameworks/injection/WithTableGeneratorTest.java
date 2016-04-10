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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import javaee.samples.frameworks.injection.tablegenerator.EntityWithTableGenerator;

import javax.inject.Inject;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(InjectionRunner.class)
public class WithTableGeneratorTest {
    @Rule
    @Inject
    public JPARule jpa;

    @Test
    @PersistenceContext(unitName = "table-generator-pu", properties = {
            @PersistenceProperty(name = "hibernate.id.new_generator_mappings", value = "true")/*,
            @PersistenceProperty(name = "hibernate.id.optimizer.pooled.prefer_lo", value = "false")*/
    })
    public void test() throws Exception {
        nextInsert();
        BigInteger id = nextSequenceId();
        BigInteger aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));

        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));

        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));

        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));

        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
        nextInsert();
        id = nextSequenceId();
        aid = approximatedSequenceId();
        assertThat(aid, is(id));
    }

    private void nextInsert() {
        jpa.$((em) -> {
            em.persist(new EntityWithTableGenerator());
        });
    }

    private BigInteger nextSequenceId() {
        jpa.getCurrentEntityManager().close();
        return  (BigInteger) jpa.getCurrentEntityManager()
                .createNativeQuery("SELECT IDS_VALUE FROM SHR_ID_SEQUENCES WHERE IDS_KEY = 'EWTG'")
                .getSingleResult();
    }

    private BigInteger approximatedSequenceId() {
        int maxId = jpa.createEntityManager().createQuery("select max(id) from EntityWithTableGenerator", Integer.class).getSingleResult();
        double seqId = 1 + 3 * (1 + 1 + Math.max(Math.floor((maxId - 1) / 3), 0));
        return new BigInteger((int) seqId + "");
    }
}
