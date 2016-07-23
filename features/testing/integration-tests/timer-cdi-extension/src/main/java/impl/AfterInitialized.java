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
package impl;

import timer.AbstractExtensionOnStartupTimer;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AfterInitialized extends AbstractExtensionOnStartupTimer<Job> {
    /**
     * No injection points and resources can be observed in CDI Extension.
     */

    /*
    @Resource
    ContextService proxy;
    */

    /*
    @Inject
    Job job;
    */

    /*
    @Resource//(lookup = "java:comp/DefaultManagedScheduledExecutorService")
    ManagedScheduledExecutorService executor;
    */

    @Override
    protected TimeUnit getTimeUnit() {
        return SECONDS;
    }

    @Override
    protected long getPeriodTime() {
        return 1;
    }

    @Override
    protected Class<Job> getJobType() {
        return Job.class;
    }
}
