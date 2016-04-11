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
import audit.domain.AuditChange;
import audit.domain.AuditFlow;
import audit.domain.AuditHeader;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractAuditValidationTest {
    private Audit audit;
    private AuditFlow auditFlow;
    private AuditHeader auditHeader;
    private AuditChange auditChange;

    protected abstract Validator validator();

    @Before
    public void createEmptyAudit() {
        audit = new Audit();
        auditFlow = new AuditFlow();
        auditHeader = new AuditHeader();
        auditChange = new AuditChange();
    }

    @Test
    public void shouldFailAuditValidation$1() {
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .hasSize(3);
    }

    @Test
    public void shouldFailAuditValidation$2() {
        audit.getFlows();
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .hasSize(3);
    }

    @Test
    public void shouldFailAuditValidation$3() {
        audit.setModule("");
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .hasSize(3);
    }

    @Test
    public void shouldFailAuditValidation$4() {
        audit.setModule("01234567890123456789012345678901");
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .hasSize(3);
    }

    @Test
    public void shouldFailAuditValidation$5() {
        String desc = "";
        for (int i = 0; i < 256; i++) {
            desc += " ";
        }
        audit.setDescription(desc);
        audit.setModule(" ");
        audit.setRequest(randomUUID());
        audit.getFlows().add(new AuditFlow());
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .hasSize(1);
    }

    @Test
    public void shouldValidateAudit$1() {
        audit.setRequest(randomUUID());
        audit.setModule("0123456789012345678901234567890");
        audit.getFlows().add(new AuditFlow());
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldValidateAudit$2() {
        audit.setRequest(randomUUID());
        audit.setModule("0123456789012345678901234567890");
        String desc = "";
        for (int i = 0; i < 255; i++) {
            desc += " ";
        }
        audit.setDescription(desc);
        audit.getFlows().add(new AuditFlow());
        Set<ConstraintViolation<Audit>> constraintViolations = validator().validate(audit);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldFailAuditFlowValidation$1() {
        auditFlow.setError("");
        Set<ConstraintViolation<AuditFlow>> constraintViolations = validator().validate(auditFlow);
        assertThat(constraintViolations)
                .hasSize(1);
    }

    @Test
    public void shouldValidateAuditFlow$1() {
        Set<ConstraintViolation<AuditFlow>> constraintViolations = validator().validate(auditFlow);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldValidateAuditFlow$2() {
        auditFlow.setError("e");
        Set<ConstraintViolation<AuditFlow>> constraintViolations = validator().validate(auditFlow);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldValidateAuditHeader$1() {
        Set<ConstraintViolation<AuditHeader>> constraintViolations = validator().validate(auditHeader);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldValidateAuditHeader$2() {
        auditHeader.setKey("");
        Set<ConstraintViolation<AuditHeader>> constraintViolations = validator().validate(auditHeader);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldValidateAuditHeader$3() {
        auditHeader.setValue("");
        Set<ConstraintViolation<AuditHeader>> constraintViolations = validator().validate(auditHeader);
        assertThat(constraintViolations)
                .isEmpty();
    }

    @Test
    public void shouldFailAuditChangeValidation$1() {
        Set<ConstraintViolation<AuditChange>> constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .hasSize(1);
    }

    @Test
    public void shouldFailAuditChangeValidation$2() {
        auditChange.setKey("");
        Set<ConstraintViolation<AuditChange>> constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .hasSize(1);
    }

    @Test
    public void shouldValidateAuditChange$1() {
        auditChange.setKey("k");
        Set<ConstraintViolation<AuditChange>> constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .isEmpty();

        auditChange.setNewValue("");
        constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .isEmpty();

        auditChange.setNewValue("nv");
        constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .isEmpty();

        auditChange.setOldValue("");
        constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .isEmpty();

        auditChange.setOldValue("ov");
        constraintViolations = validator().validate(auditChange);
        assertThat(constraintViolations)
                .isEmpty();
    }
}
