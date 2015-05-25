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

import org.junit.Test;
import org.junit.runner.Description;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DescriptionDatabaseTest {
    @Test
    public void shouldParseClassOnly() {
        Description description = Description.createTestDescription("pkg.Test", "test5_Method$10[2]");
        String db = JPARule.buildDatabaseFileName(description, false);
        assertThat(db, is("pkg_Test"));
        description = Description.createTestDescription("pkg.Test", " test5_ Method$10[2]");
        db = JPARule.buildDatabaseFileName(description, false);
        assertThat(db, is("pkg_Test"));
    }

    @Test
    public void shouldParseLegalDescription() {
        Description description = Description.createTestDescription("pkg.Test", "test5_Method$10[2]");
        String db = JPARule.buildDatabaseFileName(description, true);
        assertThat(db, is("pkg_Test__test5_Method_10"));
    }

    @Test
    public void shouldParseDescriptionWithWhitespaces() {
        Description description = Description.createTestDescription("pkg.Test", " test5_ Method$10[2]");
        String db = JPARule.buildDatabaseFileName(description, true);
        assertThat(db, is("pkg_Test__test5_Method_10"));
    }

}
