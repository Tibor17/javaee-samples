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

import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;

import javax.enterprise.inject.Vetoed;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.FetchType.EAGER;

@Vetoed
@Entity
@Table(name = "AUDIT")
@Access(FIELD)
@Indexed
public class Audit extends BaseEntity {
    @Column(name = "REQUEST_UUID", columnDefinition = "varchar(36)", nullable = false, updatable = false)
    //@Convert(converter = UuidConverter.class)
    @NotNull
    @Basic
    private UUID request;

    @Column(name = "INITIATOR", precision = 19, nullable = false, updatable = false)
    @NotNull
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.YES, termVector = TermVector.YES)
    private long initiator;

    @Column(name = "MODULE", length = 31, nullable = false, updatable = false)
    @NotNull
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.YES, termVector = TermVector.YES)
    private String module;

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "FK_AUDIT", nullable = false, updatable = false)
    @org.hibernate.annotations.ForeignKey(name = "FK_AUDIT__FLOW")
    @Size(min = 1)
    private List<AuditFlow> flows;

    public UUID getRequest() {
        return request;
    }

    public void setRequest(UUID request) {
        this.request = request;
    }

    public long getInitiator() {
        return initiator;
    }

    public void setInitiator(long initiator) {
        this.initiator = initiator;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public List<AuditFlow> getFlows() {
        if (flows == null) {
            flows = new ArrayList<>();
        }
        return flows;
    }
}
