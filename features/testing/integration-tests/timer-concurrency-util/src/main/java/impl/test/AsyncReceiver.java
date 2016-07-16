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

import javax.annotation.Resource;
import javax.ejb.*;
import java.util.concurrent.Future;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;

@Stateless
@LocalBean
public class AsyncReceiver {
    @Resource
    SessionContext ctx;

    @Asynchronous
    public Future<String> forkTask() {
        pretendJob();
        out.println("forTask() cancelled " + ctx.wasCancelCalled());
        return new AsyncResult<>(ctx.wasCancelCalled() ? null : "done");
    }

    private void pretendJob() {
        try {
            SECONDS.sleep(1);
        } catch (InterruptedException e) {
            // keep interrupted status - pretends real long standing computations not throwing this exception
            Thread.currentThread().interrupt();
        }
    }
}
