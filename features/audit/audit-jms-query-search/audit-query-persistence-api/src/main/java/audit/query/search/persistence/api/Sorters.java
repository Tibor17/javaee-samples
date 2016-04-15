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

import audit.query.search.api.Sorter;

import java.util.Calendar;

public final class Sorters {
    private Sorters() {
        throw new IllegalStateException("no instantiable constructor");
    }

    public static Sorter<String> module(boolean ascending) {
        return new Sorter<>("sortModule", ascending, String.class);
    }

    public static Sorter<String> module() {
        return new Sorter<>("sortModule", true, String.class);
    }

    public static Sorter<Long> initiator(boolean ascending) {
        return new Sorter<>("sortInitiator", ascending, Long.class);
    }

    public static Sorter<Long> initiator() {
        return new Sorter<>("sortInitiator", true, Long.class);
    }

    public static Sorter<Calendar> storedAt(boolean ascending) {
        return new Sorter<>("sortStoredAt", ascending, Calendar.class);
    }

    public static Sorter<Calendar> storedAt() {
        return new Sorter<>("sortStoredAt", true, Calendar.class);
    }

    public static Sorter<String> description(boolean ascending) {
        return new Sorter<>("sortDescription", ascending, String.class);
    }

    public static Sorter<String> description() {
        return new Sorter<>("sortDescription", true, String.class);
    }

    public static Sorter<String> error(boolean ascending) {
        return new Sorter<>("sortError", ascending, String.class);
    }

    public static Sorter<String> error() {
        return new Sorter<>("sortError", true, String.class);
    }
}
