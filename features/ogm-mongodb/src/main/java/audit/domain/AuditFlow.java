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
package audit.domain;

import javax.enterprise.inject.Vetoed;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.FetchType.EAGER;

@Vetoed
@Entity
@Table(name = "AUDIT_FLOW")
@Access(FIELD)
public class AuditFlow extends BaseEntity {
    @Column(name = "ERROR", updatable = false)
    private String error;

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "FK_AUDIT_FLOW", nullable = false, updatable = false)
    @org.hibernate.annotations.ForeignKey(name = "FK_AUDIT_FLOW__HEADER")
    private List<AuditHeader> headers;

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "FK_AUDIT_FLOW", nullable = false, updatable = false)
    @org.hibernate.annotations.ForeignKey(name = "FK_AUDIT_FLOW__CHANGE")
    private List<AuditChange> changes;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<AuditHeader> getHeaders() {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        return headers;
    }

    public List<AuditChange> getChanges() {
        if (changes == null) {
            changes = new ArrayList<>();
        }
        return changes;
    }
}
