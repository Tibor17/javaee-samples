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
package javaee.samples.frameworks.junitjparule;

enum H2Storage {
    DEFAULT_STORAGE,

    /*
    * For H2 version 1.4 and newer, the MVStore is the default storage engine (supporting SQL, JDBC, transactions, MVCC, and so on).
    * For older versions, append ;MV_STORE=TRUE to the database URL. Even though it can be used with the default table level
    * locking, by default the MVCC mode is enabled when using the MVStore.
    * */
    ENABLE_MV_STORE, DISABLE_MV_STORE,

    /**
     * Please note MVCC is enabled in version 1.4.x by default, when using the MVStore. In this case, table level locking is not used.
     * Instead, rows are locked for update, and read committed is used in all cases (changing the isolation level has no effect).
     */
    ENABLE_MVCC,

    MULTI_THREADED_1;
}
