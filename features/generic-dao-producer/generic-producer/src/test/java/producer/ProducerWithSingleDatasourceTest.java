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

import dao.DAO;
import dao.IDAO;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class, projectStage = ProjectStage.Development.class,
        startExternalContainers = false)
@Vetoed
public class ProducerWithSingleDatasourceTest {
    @Inject
    @DAO
    IDAO<MyEntity, Long> dao;

    @Inject
    TransactionalDeltaspikeHelper helper;

    @Before
    public void cleanupDatabase() {
        dao.loadAll()
                .stream()
                .forEach(e -> helper.$(() -> dao.delete(e)));
    }

    @Test
    public void test() {
        assertThat(dao)
                .isNotNull();

        assertThat(dao.count())
                .isZero();

        MyEntity e = helper.$(new MyEntity().setCourseName("Java EE"));

        assertThat(e)
                .extracting(MyEntity::getCourseName)
                .containsExactly("Java EE");

        assertThat(e.getId())
                .isNotZero();

        assertThat(dao.count())
                .isNotZero();

        assertThat(dao)
                .extracting(IDAO::count)
                .containsExactly(1L);
    }
}
