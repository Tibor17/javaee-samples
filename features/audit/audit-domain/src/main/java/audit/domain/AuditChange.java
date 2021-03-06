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

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TermVector;

import javax.enterprise.inject.Vetoed;
import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.hash;
import static javax.persistence.AccessType.FIELD;

@Vetoed
@Entity
@Table(name = "AUDIT_CHANGE")
@Access(FIELD)
@Indexed
public class AuditChange extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("key", String.class),
            new ObjectStreamField("oldValue", String.class),
            new ObjectStreamField("newValue", String.class)
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
    @Column(name = "OLD_VALUE", updatable = false)
    @Field(store = Store.YES, termVector = TermVector.YES)
    @Size(max = 255)
    private String oldValue;

    /**
     * @serialField new property value of this audit change
     */
    @Column(name = "NEW_VALUE", updatable = false)
    @Field(store = Store.YES, termVector = TermVector.YES)
    @Size(max = 255)
    private String newValue;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
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
