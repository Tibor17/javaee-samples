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
package producer;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class, projectStage = ProjectStage.Development.class, startExternalContainers = false)
public class ProducerTest {
    @Inject
    MyRepository repository;

    @Inject
    TransactionalDeltaspikeHelper helper;

    @Test
    public void test() {
        assertThat(repository)
                .isNotNull();

        assertThat(repository.load())
                .isEmpty();

        assertThat(repository.findByCourse("javaee"))
                .isNull();

        MyEntity e = helper.$(new MyEntity().setCourseName("javaee"));

        assertThat(repository.load())
                .hasSize(1);

        assertThat(repository.findByCourse("javaee"))
                .isNotNull();

        assertThat(repository.findByCourseName("javaee"))
                .isNotNull();

        AnotherEntity a = helper.$(new AnotherEntity().setCourseName("javase"));

        assertThat(repository.find("javase"))
                .isNotNull();

        assertThat(repository.findOptional("javase"))
                .isNotNull();

        assertThat(repository.findAnyByCourseName("javaee"))
                .isNotNull();
    }
}
