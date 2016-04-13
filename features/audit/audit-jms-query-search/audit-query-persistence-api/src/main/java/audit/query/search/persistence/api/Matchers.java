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
package audit.query.search.persistence.api;

import audit.domain.Audit;
import audit.domain.AuditFlow;
import audit.query.search.api.Matcher;
import audit.query.search.api.StringFieldMatcher;

import javax.enterprise.inject.Vetoed;

@Vetoed
public final class Matchers {
    private Matchers() {
        throw new IllegalStateException("no instantiable constructor");
    }

    public static Matcher<Audit> module(String s) {
        return new StringFieldMatcher<>(s, "module", Audit.class);
    }

    public static Matcher<Audit> initiator(String s) {
        return new StringFieldMatcher<>(s, "initiator", Audit.class);
    }

    public static Matcher<Audit> storedAt(String s) {
        return new StringFieldMatcher<>(s, "storedAt", Audit.class);
    }

    public static Matcher<Audit> description(String s) {
        return new StringFieldMatcher<>(s, "description", Audit.class);
    }

    public static Matcher<AuditFlow> error(String s) {
        return new StringFieldMatcher<>(s, "error", AuditFlow.class);
    }
}
