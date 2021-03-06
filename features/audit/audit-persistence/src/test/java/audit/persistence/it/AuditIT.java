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
import audit.domain.AuditFlow;
import audit.domain.AuditHeader;
import audit.persistence.service.AuditService;
import javaee.samples.frameworks.injection.DatabaseConfiguration;
import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.*;

import java.util.Calendar;

import static java.lang.Integer.MAX_VALUE;
import static java.time.ZoneOffset.UTC;
import static java.util.Calendar.SECOND;
import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static java.util.TimeZone.getTimeZone;
import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static java.util.UUID.randomUUID;

@RunWith(InjectionRunner.class)
@DatabaseConfiguration(UNDEFINED)
@PersistenceContext(unitName = "audit-jpa")
@WithManagedTransactions
public class AuditIT {
    @Inject
    AuditService service;

    Audit expected;

    Calendar expectedStoredFrom, expectedStoredTo;

    @Before
    public void fillDatabase() {
        service.findAll()
                .forEach(service::remove);

        AuditHeader header = new AuditHeader();
        header.setKey("hk");
        header.setValue("hv");

        AuditChange change = new AuditChange();
        change.setKey("k");
        change.setOldValue("o");
        change.setNewValue("n");

        expected = new Audit();
        expected.setRequest(randomUUID());
        expected.setInitiator(1);
        expected.setModule("audit-module");
        expected.setOperationKey("login");
        expected.setDescription("desc");

        expectedStoredFrom = Calendar.getInstance(getTimeZone(UTC));
        expectedStoredFrom.set(Calendar.MILLISECOND, 0);
        expectedStoredTo = Calendar.getInstance(getTimeZone(UTC));
        expectedStoredTo.add(SECOND, 3);
        expectedStoredTo.set(Calendar.MILLISECOND, 0);

        service.saveFlow(expected, "some error", singleton(header), singleton(change));
    }

    @Test
    public void dates() {
        assertThat(service.search(0, MAX_VALUE, of(1L), of("audit-module"), of("login"), of("esc"), of(expectedStoredFrom), of(expectedStoredTo)))
                .hasSize(1);
    }

    @Test
    public void canPersistAndLoad() {
        Audit actual = service.findAuditById(expected.getId());

        assertThat(actual)
                .isNotSameAs(expected);

        assertThat(actual.getRequest())
                .isEqualTo(expected.getRequest());

        assertThat(actual.getInitiator())
                .isEqualTo(expected.getInitiator());

        assertThat(actual.getModule())
                .isEqualTo(expected.getModule());

        assertThat(actual.getOperationKey())
                .isEqualTo(expected.getOperationKey());

        assertThat(actual.getDescription())
                .isEqualTo(expected.getDescription());

        assertThat(actual.getStoredAt())
                .isNotNull()
                .isGreaterThanOrEqualTo(expectedStoredFrom)
                .isLessThanOrEqualTo(expectedStoredTo);

        assertThat(actual.getFlows())
                .hasSize(1);

        assertThat(actual.getFlows())
                .extracting("error", String.class)
                .containsExactly("some error");

        AuditFlow flow = actual.getFlows().get(0);

        assertThat(flow.getHeaders())
                .hasSize(1);

        assertThat(flow.getHeaders())
                .extracting(AuditHeader::getKey, AuditHeader::getValue)
                .containsSequence(tuple("hk", "hv"));

        assertThat(flow.getChanges())
                .hasSize(1);

        assertThat(flow.getChanges())
                .extracting(AuditChange::getKey, AuditChange::getOldValue, AuditChange::getNewValue)
                .containsSequence(tuple("k", "o", "n"));

        assertThat(service.searchAuditPhrase(0, MAX_VALUE, p -> p.matchModule("audit-module")))
                .hasSize(1);

        assertThat(service.searchAuditPhrase(0, MAX_VALUE, p -> p.matchModule("audit-moduleX")))
                .hasSize(0);

        assertThat(service.searchAuditLike(0, MAX_VALUE, p -> p.matchModule("audit-moduleX")))
                .hasSize(1);

        assertThat(service.searchAuditLike(0, MAX_VALUE, p -> p.matchModule("audit")))
                .hasSize(1);

        assertThat(service.searchAuditLike(0, MAX_VALUE, p -> p.matchModule("module")))
                .hasSize(1);

        assertThat(service.searchAuditFlowPhrase("some error", 0, MAX_VALUE))
                .hasSize(1);
    }
}
