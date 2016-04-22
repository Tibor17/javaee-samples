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
package audit.query.search.api;

import org.junit.Test;

import java.io.*;
import java.util.Calendar;

import static audit.query.search.api.SortField.SORT_BY_DATE;
import static audit.util.Dates.toXMLGregorianCalendar;
import static java.util.Calendar.SECOND;
import static org.assertj.core.api.Assertions.assertThat;

public class SerializationTest {

    @Test
    public void serializeAuditQuery() throws Exception {
        AuditQuery query = new AuditQuery();
        query.setOperationKey("operation");
        query.setDescription("desc");
        query.setInitiator(5L);
        query.setModule("mod");
        query.setError("err");
        query.setSortField(SORT_BY_DATE);
        query.setStartRowNum(10);
        query.setPageSize(20);
        query.setFrom(toXMLGregorianCalendar(Calendar.getInstance()));
        Calendar to = Calendar.getInstance();
        to.add(SECOND, 1);
        query.setTo(toXMLGregorianCalendar(to));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream serializer = new ObjectOutputStream(stream);
        serializer.writeObject(query);
        serializer.flush();

        ObjectInputStream deserializer = new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
        AuditQuery actual = (AuditQuery) deserializer.readObject();

        assertThat(actual)
                .extracting(AuditQuery::getOperationKey)
                .containsExactly(actual.getOperationKey());

        assertThat(actual)
                .extracting(AuditQuery::getDescription)
                .containsExactly(actual.getDescription());

        assertThat(actual)
                .extracting(AuditQuery::getInitiator)
                .containsExactly(actual.getInitiator());

        assertThat(actual)
                .extracting(AuditQuery::getModule)
                .containsExactly(actual.getModule());

        assertThat(actual)
                .extracting(AuditQuery::getError)
                .containsExactly(actual.getError());

        assertThat(actual)
                .extracting(AuditQuery::getStartRowNum)
                .containsExactly(actual.getStartRowNum());

        assertThat(actual)
                .extracting(AuditQuery::getPageSize)
                .containsExactly(actual.getPageSize());

        assertThat(actual)
                .extracting(AuditQuery::getModule)
                .containsExactly(actual.getModule());

        assertThat(actual)
                .extracting(AuditQuery::getFrom)
                .containsExactly(actual.getFrom());

        assertThat(actual)
                .extracting(AuditQuery::getTo)
                .containsExactly(actual.getTo());
    }
}
