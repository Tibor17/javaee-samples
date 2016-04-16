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
package audit.jms.unit;

import audit.domain.Audit;
import audit.jms.consumer.AuditListener;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class Listener implements AuditListener {
    private final CyclicBarrier synchronizer;

    private Listener() {
        this(null);
    }

    public Listener(CyclicBarrier synchronizer) {
        this.synchronizer = synchronizer;
    }

    @Override
    public void onMessage(Audit audit) {
        try {
            synchronizer.await(3, SECONDS);

            assertThat(audit.getModule())
                    .isEqualTo("test");

            assertThat(audit.getOperationKey())
                    .isEqualTo("login");

        } catch (TimeoutException | BrokenBarrierException | InterruptedException e) {
            fail("Too overloaded build system. Could not acquire permit within 3 seconds. "
                    + e.getLocalizedMessage(), e);
        }
    }
}
