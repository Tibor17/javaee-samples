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
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.hash;
import static javax.persistence.AccessType.FIELD;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;

@Vetoed
@Entity
@Table(name = "AUDIT_CHANGE")
@Access(FIELD)
public class AuditChange extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("key", String.class),
            new ObjectStreamField("oldValue", AuditChangeValue.class),
            new ObjectStreamField("newValue", AuditChangeValue.class)
    };

    /**
     * @serialField property key of this audit change
     */
    @Column(name = "KEY", nullable = false, updatable = false)
    @NotNull
    @Size(min = 1, max = 255)
    private String key;

    /**
     * @serialField old property value of this change
     */
    @OneToOne(optional = false, orphanRemoval = true, cascade = {PERSIST, REFRESH, REMOVE})
    @JoinColumn(name = "OLD_VALUE", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_AUDIT_CHANGE__OLD_VALUE"))
    @NotNull
    private AuditChangeValue oldValue;

    /**
     * @serialField new property value of this audit change
     */
    @OneToOne(optional = false, orphanRemoval = true, cascade = {PERSIST, REFRESH, REMOVE})
    @JoinColumn(name = "NEW_VALUE", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_AUDIT_CHANGE__NEW_VALUE"))
    @NotNull
    private AuditChangeValue newValue;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AuditChangeValue getOldValue() {
        return oldValue;
    }

    public void setOldValue(AuditChangeValue oldValue) {
        this.oldValue = oldValue;
    }

    public AuditChangeValue getNewValue() {
        return newValue;
    }

    public void setNewValue(AuditChangeValue newValue) {
        this.newValue = newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditChange that = (AuditChange) o;
        return Objects.equals(getKey(), that.getKey())
                && Objects.equals(getOldValue(), that.getOldValue())
                && Objects.equals(getNewValue(), that.getNewValue());
    }

    @Override
    public int hashCode() {
        return hash(getKey(), getOldValue(), getNewValue());
    }
}
