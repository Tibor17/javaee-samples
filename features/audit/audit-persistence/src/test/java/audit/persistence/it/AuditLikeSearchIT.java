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
package audit.persistence.it;

import audit.domain.Audit;
import audit.domain.AuditChange;
import audit.domain.AuditHeader;
import audit.persistence.service.AuditService;
import javaee.samples.frameworks.injection.DatabaseConfiguration;
import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.PersistenceContext;

import java.util.Calendar;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Calendar.HOUR;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(InjectionRunner.class)
@DatabaseConfiguration(UNDEFINED)
@PersistenceContext(unitName = "audit-jpa")
@WithManagedTransactions
public class AuditLikeSearchIT {
    @Inject
    AuditService service;

    @Before
    public void fillDatabase() {
        service.findAll()
                .forEach(service::remove);

        newRecord(1, "abc");
        newRecord(2, "c-prefix");
        newRecord(3, "a-prefix");
        newRecord(4, "b-prefix");
        newRecord(5, "xyz");
    }

    @Test
    public void shouldFilterAndSortModule() {
        List<Audit> actual = service.searchAuditLike(0, MAX_VALUE, p -> p.matchModule("prefix").sortModule());
        assertThat(actual)
                .hasSize(3)
                .extracting(Audit::getModule)
                .containsSequence("a-prefix", "b-prefix", "c-prefix");
    }

    private void newRecord(int hoursDrift, String module) {
        Calendar now = Calendar.getInstance();
        now.add(HOUR, hoursDrift);

        AuditHeader header = new AuditHeader();
        header.setKey("hk");
        header.setValue("hv");

        AuditChange change = new AuditChange();
        change.setKey("k");
        change.setOldValue("o");
        change.setNewValue("n");

        Audit expected = new Audit();
        expected.setRequest(randomUUID());
        expected.setInitiator(1);
        expected.setModule(module);
        expected.setOperationKey("login");
        expected.setDescription("desc");

        service.saveFlow(expected, "some error", singleton(header), singleton(change));
    }
}
