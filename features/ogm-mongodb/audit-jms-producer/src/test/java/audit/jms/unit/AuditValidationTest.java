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

import org.junit.BeforeClass;

import javax.enterprise.inject.Vetoed;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.setDefault;
import static javax.validation.Validation.buildDefaultValidatorFactory;

@Vetoed
public class AuditValidationTest extends AbstractAuditValidationTest {
    private static volatile Validator validator;

    @BeforeClass
    public static void setUp() {
        setDefault(ENGLISH);
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Override
    protected Validator validator() {
        return validator;
    }
}
