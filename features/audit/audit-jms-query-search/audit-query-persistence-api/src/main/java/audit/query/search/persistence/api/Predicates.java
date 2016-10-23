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

import audit.query.search.api.Matcher;
import audit.query.search.api.Sorter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import static audit.query.search.persistence.api.Matchers.description;
import static audit.query.search.persistence.api.Matchers.error;
import static audit.query.search.persistence.api.Matchers.initiator;
import static audit.query.search.persistence.api.Matchers.module;
import static audit.query.search.persistence.api.Matchers.storedAt;
import static audit.query.search.persistence.api.Sorters.description;
import static audit.query.search.persistence.api.Sorters.error;
import static audit.query.search.persistence.api.Sorters.initiator;
import static audit.query.search.persistence.api.Sorters.module;
import static audit.query.search.persistence.api.Sorters.storedAt;

public final class Predicates {
    private final Collection<Matcher<?>> matchers = new ArrayList<>();
    private final Collection<Sorter<?>> sorters = new ArrayList<>();

    private Predicates() {
    }

    public static Predicates predicates() {
        return new Predicates();
    }

    public Collection<Matcher<?>> getMatchers() {
        return matchers;
    }

    public Collection<Sorter<?>> getSorters() {
        return sorters;
    }

    public <T> Predicates match(Matcher<T> matcher) {
        matchers.add(matcher);
        return this;
    }

    public <T extends Serializable & Comparable<T>> Predicates sort(Sorter<T> sorter) {
        sorters.add(sorter);
        return this;
    }

    public Predicates matchModule(String txt) {
        return match(module(txt));
    }

    public Predicates sortModule() {
        return sort(module());
    }

    public Predicates sortModuleDesc() {
        return sort(module(false));
    }

    public Predicates matchInitiator(long txt) {
        return match(initiator(txt));
    }

    public Predicates sortInitiator() {
        return sort(initiator());
    }

    public Predicates sortInitiatorDesc() {
        return sort(initiator(false));
    }

    public Predicates matchStoredAt(Calendar txt) {
        return match(storedAt(txt));
    }

    public Predicates sortStoredAt() {
        return sort(storedAt());
    }

    public Predicates sortStoredAtDesc() {
        return sort(storedAt(false));
    }

    public Predicates matchDescription(String txt) {
        return match(description(txt));
    }

    public Predicates sortDescription() {
        return sort(description());
    }

    public Predicates sortDescriptionDesc() {
        return sort(description(false));
    }

    public Predicates matchError(String txt) {
        return match(error(txt));
    }

    public Predicates sortError() {
        return sort(error());
    }

    public Predicates sortErrorDesc() {
        return sort(error(false));
    }
}
