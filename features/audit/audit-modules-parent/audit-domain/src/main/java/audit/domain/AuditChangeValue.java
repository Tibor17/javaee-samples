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
import javax.validation.constraints.NotNull;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static audit.domain.AuditChangeValueType.*;
import static java.nio.ByteBuffer.allocate;
import static java.util.Arrays.deepHashCode;
import static javax.persistence.AccessType.FIELD;
import static javax.persistence.EnumType.STRING;

@Vetoed
@Entity
@Table(name = "AUDIT_CHANGE_VALUE")
@Access(FIELD)
public class AuditChangeValue extends BaseEntity implements Serializable {
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
        return value == null ? null : value.clone();
    }

    public String getValueAsString() {
        if (valueType != AuditChangeValueType.STRING)
            throw new IllegalStateException("expected value-type STRING");
        return value == null ? null : new String(value);
    }

    public Boolean getValueAsBoolean() {
        if (valueType != BOOLEAN || value != null && value.length != 1)
            throw new IllegalStateException("expected value-type BOOLEAN");
        return value == null ? null : value[0] != 0;
    }

    public Byte getValueAsByte() {
        if (valueType != BYTE || value != null && value.length != 1)
            throw new IllegalStateException("expected value-type BYTE");
        return value == null ? null : value[0];
    }

    public Short getValueAsShort() {
        if (valueType != SHORT || value != null && value.length != 2)
            throw new IllegalStateException("expected value-type SHORT");
        return value == null ? null : allocate(2).get(value).getShort();
    }

    public Integer getValueAsInteger() {
        if (valueType != INTEGER || value != null && value.length != 4)
            throw new IllegalStateException("expected value-type INTEGER");
        return value == null ? null : allocate(4).get(value).getInt();
    }

    public Long getValueAsLong() {
        if (valueType != LONG || value != null && value.length != 8)
            throw new IllegalStateException("expected value-type LONG");
        return value == null ? null : allocate(8).get(value).getLong();
    }

    public Float getValueAsFloat() {
        if (valueType != FLOAT || value != null && value.length != 4)
            throw new IllegalStateException("expected value-type FLOAT");
        return value == null ? null : allocate(4).get(value).getFloat();
    }

    public Double getValueAsDouble() {
        if (valueType != DOUBLE || value != null && value.length != 8)
            throw new IllegalStateException("expected value-type DOUBLE");
        return value == null ? null : allocate(8).get(value).getDouble();
    }

    private AuditChangeValue setValue(AuditChangeValueType valueType, byte[] value) {
        this.valueType = valueType;
        this.value = value.clone();
        return this;
    }

    public AuditChangeValue setValue(String value) {
        valueType = AuditChangeValueType.STRING;
        this.value = value == null ? null : value.getBytes();
        return this;
    }

    public AuditChangeValue setValue(boolean value) {
        valueType = BOOLEAN;
        this.value = new byte[]{(byte) (value ? 1 : 0)};
        return this;
    }

    public AuditChangeValue setValue(Byte value) {
        valueType = BYTE;
        this.value = value == null ? null : new byte[] {value};
        return this;
    }

    public AuditChangeValue setValue(Short value) {
        valueType = SHORT;
        this.value = value == null ? null : allocate(2).putShort(value).array();
        return this;
    }

    public AuditChangeValue setValue(Integer value) {
        valueType = INTEGER;
        this.value = value == null ? null : allocate(4).putInt(value).array();
        return this;
    }

    public AuditChangeValue setValue(Long value) {
        valueType = LONG;
        this.value = value == null ? null : allocate(8).putLong(value).array();
        return this;
    }

    public AuditChangeValue setValue(Float value) {
        valueType = FLOAT;
        this.value = value == null ? null : allocate(4).putFloat(value).array();
        return this;
    }

    public AuditChangeValue setValue(Double value) {
        valueType = DOUBLE;
        this.value = value == null ? null : allocate(8).putDouble(value).array();
        return this;
    }

    public AuditChangeValue setValue(byte[] value) {
        return setValue(BYTE_ARRAY, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditChangeValue that = (AuditChangeValue) o;
        return Objects.equals(getValueType(), that.getValueType())
                && Objects.equals(getDiscriminator(), that.getDiscriminator())
                && Arrays.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        Object[] args = {getValueType(), getDiscriminator(), getValue()};
        return deepHashCode(args);
    }
}
