package audit.domain;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;

import java.util.Objects;

import static java.util.Objects.hash;
import static javax.persistence.AccessType.FIELD;

@Entity(name = "AUDIT_HEADER")
@Access(FIELD)
public class AuditHeader extends BaseEntity {
    @Column(name = "KEY", updatable = false)
    private String key;

    @Column(name = "VALUE", updatable = false)
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditHeader that = (AuditHeader) o;
        return Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return hash(getKey(), getValue());
    }
}
