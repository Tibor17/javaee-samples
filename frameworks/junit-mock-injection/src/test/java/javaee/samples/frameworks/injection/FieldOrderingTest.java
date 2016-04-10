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
package javaee.samples.frameworks.injection;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static javaee.samples.frameworks.injection.InjectionRunner.orderFields;
import static org.assertj.core.api.Assertions.*;

public class FieldOrderingTest {
    @InjectionPointOrdinal(3)
    int f3;

    @InjectionPointOrdinal(1)
    int f1;

    @InjectionPointOrdinal(2)
    int f2;

    @Test
    public void shouldOrder() {
        Collection<String> orderedFieldNames =
                orderFields(FieldOrderingTest.class.getDeclaredFields())
                        .stream()
                        .map(Field::getName)
                        .collect(toList());

        assertThat(orderedFieldNames)
                .containsSequence("f1", "f2", "f3");
    }
}
