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

import org.hibernate.search.annotations.Indexed;

import javax.enterprise.inject.Vetoed;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.ObjectStreamField;
import java.io.Serializable;

import static audit.domain.AuditChangeValueType.*;
import static java.nio.ByteBuffer.allocate;
import static javax.persistence.AccessType.FIELD;
import static javax.persistence.EnumType.STRING;

@Vetoed
@Entity
@Table(name = "AUDIT_CHANGE_VALUE")
@Access(FIELD)
@Indexed
public class AuditChangeValue implements Serializable {
    private static final long serialVersionUID = 1;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("valueType", AuditChangeValueType.class),
            new ObjectStreamField("discriminator", String.class),
            new ObjectStreamField("value", byte[].class)
    };

    @Column(name = "VALUE_TYPE", nullable = false, updatable = false, length = 15)
    @Enumerated(STRING)
    @NotNull
    private AuditChangeValueType valueType;

    /**
     * This represents the purpose of stored value
     * with proprietary syntax with limited
     * string-length of 255 characters; e.g. unlocalized-value,
     * localized-property-key, id-of-record-in-sql-table::TABLE.
     */
    @Column(name = "DISCRIMINATOR", nullable = false, updatable = false)
    @NotNull
    private String discriminator;

    @Lob @Basic
    @Column(name = "VALUE", updatable = false)
    private byte[] value;

    public AuditChangeValueType getValueType() {
        return valueType;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public AuditChangeValue setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
        return this;
    }

    public byte[] getValue() {
        return value;
    }

    public AuditChangeValue setValue(AuditChangeValueType valueType, byte[] value) {
        this.valueType = valueType;
        this.value = value.clone();
        return this;
    }

    public AuditChangeValue setValue(String value) {
        this.valueType = AuditChangeValueType.STRING;
        this.value = value == null ? null : value.getBytes();
        return this;
    }

    public AuditChangeValue setValue(Integer value) {
        this.valueType = INTEGER;
        this.value = value == null ? null : allocate(4).putInt(value).array();
        return this;
    }

    public AuditChangeValue setValue(Long value) {
        this.valueType = LONG;
        this.value = value == null ? null : allocate(8).putLong(value).array();
        return this;
    }

    public AuditChangeValue setValue(byte[] value) {
        return setValue(BYTE_ARRAY, value);
    }
}
