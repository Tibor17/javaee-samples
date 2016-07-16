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
package impl.test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import java.util.concurrent.Future;

import static java.lang.System.err;
import static javax.ejb.ConcurrencyManagementType.BEAN;

@Singleton
@Startup
@ConcurrencyManagement(BEAN)
public class SyncCaller {
    @Inject
    AsyncReceiver rx;

    volatile Future<String> forkedTask;

    @PostConstruct
    public void onConstruct() {
        err.println("SyncCaller#onConstruct() before");
        forkedTask = rx.forkTask();
        //forkedTask.cancel(true);
        err.println("SyncCaller#onConstruct() after");
    }

    @PreDestroy
    public void onDestroy() {
        err.println("SyncCaller CANCELLED " + forkedTask.isCancelled());
        forkedTask.cancel(true);
        err.println("SyncCaller CANCELLED " + forkedTask.isCancelled());
    }
}
