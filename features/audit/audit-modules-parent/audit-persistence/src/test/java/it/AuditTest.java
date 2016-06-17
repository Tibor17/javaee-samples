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
package it;

import audit.domain.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import service.AuditService;
import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.TimeZone;

import static java.lang.Integer.MAX_VALUE;
import static java.time.ZoneOffset.UTC;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.SECOND;
import static java.util.Collections.singleton;
import static java.util.Locale.ROOT;
import static java.util.Optional.of;
import static java.util.TimeZone.getTimeZone;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(InjectionRunner.class)
@PersistenceContext(unitName = "audit-jpa")
@WithManagedTransactions
public class AuditTest {
    @Inject
    AuditService service;

    Audit expected;

    Calendar expectedStoredFrom, expectedStoredTo;
    AuditHeader header;
    AuditChange change;

    @Before
    public void fillDatabase() {
        TimeZone.setDefault(getTimeZone(UTC));

        service.findAll()
                .forEach(service::remove);

        header = new AuditHeader();
        header.setKey("hk");
        header.setValue("hv");

        change = new AuditChange();
        change.setKey("k");
        change.setOldValue(new AuditChangeValue().setValue("o").setDiscriminator("x"));
        change.setNewValue(new AuditChangeValue().setValue("n").setDiscriminator("x"));

        expected = new Audit();
        expected.setRequest(randomUUID());
        expected.setInitiator(1);
        expected.setModule("audit-module");
        expected.setOperationKey("login");
        expected.setDescription("desc");
    }

    private void prepareData(int fromSeconds, int toSeconds) {
        expectedStoredFrom = Calendar.getInstance(getTimeZone(UTC));
        expectedStoredFrom.add(SECOND, fromSeconds);
        expectedStoredFrom.set(MILLISECOND, 0);

        expectedStoredTo = Calendar.getInstance(getTimeZone(UTC));
        expectedStoredTo.add(SECOND, toSeconds);
        expectedStoredTo.set(MILLISECOND, 0);

        service.saveFlow(expected, "some error", singleton(header), singleton(change));
    }

    @Test
    public void dates() {
        prepareData(-3, 3);

        assertThat(service.search(0, MAX_VALUE, of(1L), of("audit-module"), of("login"), of("esc"),
                of(expectedStoredFrom), of(expectedStoredTo)))
                .hasSize(1);
    }

    @Test
    public void negativeTest() {
        prepareData(-3, -1);

        assertThat(service.search(0, MAX_VALUE, of(1L), of("audit-module"), of("login"), of("esc"),
                of(expectedStoredFrom), of(expectedStoredTo)))
                .isEmpty();
    }

    @Test
    public void canPersistAndLoad() {
        prepareData(-3, 3);

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

        assertThat(toSystemZone(actual.getStoredAt()))
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
                .extracting(AuditChange::getKey, AuditTest::toOldValue, AuditTest::toNewValue)
                .containsSequence(tuple("k", "o", "n"));
    }

    private static Calendar toSystemZone(Calendar utdDate)  {
        DateTime dateTime = new DateTime(utdDate, DateTimeZone.UTC);
        return dateTime.toCalendar(ROOT);
    }

    private static String toOldValue(AuditChange c) {
        return new String(c.getOldValue().getValue());
    }

    private static String toNewValue(AuditChange c) {
        return new String(c.getNewValue().getValue());
    }
}
