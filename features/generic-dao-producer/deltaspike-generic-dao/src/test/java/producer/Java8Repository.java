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

import org.apache.deltaspike.data.api.EntityManagerConfig;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

@Repository(forEntity = MyEntity.class)
@EntityManagerConfig(entityManagerResolver = CrmEntityManagerResolver.class)
public interface Java8Repository {

    @Query(named = "delete.all")
    @Modifying
    void deleteAll();

    @Query("select e from MyEntity e")
    List<MyEntity> loadAll();

    @Query(value = "select e from MyEntity e where e.courseName = ?1", singleResult = OPTIONAL)
    MyEntity findByCourse(@NotNull String courseName);

    MyEntity findByCourseName(@NotNull String courseName);

    @Query(value = "select e from AnotherEntity e where e.courseName = ?1", singleResult = OPTIONAL)
    AnotherEntity find(@NotNull String courseName);

    @Query("select e from AnotherEntity e where e.courseName = ?1")
    AnotherEntity findOptional(@NotNull String courseName);

    MyEntity findAnyByCourseName(@NotNull String courseName);

    default AggregateRoot loadAggregateRoot(String courseName) {
        MyEntity myEntity = findAnyByCourseName(courseName);
        AnotherEntity anotherEntity = find(courseName);
        return new AggregateRoot(myEntity, anotherEntity);
    }
}
