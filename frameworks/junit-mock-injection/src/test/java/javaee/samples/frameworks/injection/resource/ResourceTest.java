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
package javaee.samples.frameworks.injection.resource;

import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Session;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(InjectionRunner.class)
@WithManagedTransactions
public class ResourceTest {
    @Produces
    Session session = mock(Session.class);

    @Inject
    Session in;

    @Resource
    static Session staticResource;

    @Resource
    static Session thisResource;

    @Inject
    SessionService service;

    @Test
    public void shouldInjectResource() {
        assertThat(in)
                .isNotNull();

        assertThat(staticResource)
                .isNotNull();

        assertThat(thisResource)
                .isNotNull();

        assertThat(service)
                .isNotNull();

        assertThat(service)
                .extracting(SessionService::getSession)
                .doesNotContainNull();

        assertThat(service)
                .extracting(SessionService::getStaticResource)
                .doesNotContainNull();

        assertThat(service)
                .extracting(SessionService::getThisResource)
                .doesNotContainNull();
    }
}
