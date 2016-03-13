package audit.domain;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import java.util.Objects;

import static java.util.Objects.hash;
import static javax.persistence.AccessType.FIELD;

@Entity(name = "AUDIT_CHANGE")
@Access(FIELD)
public class AuditChange extends BaseEntity {
    @Column(name = "KEY", nullable = false, updatable = false)
    @NotNull
    private String key;

    @Column(name = "OLD_VALUE", updatable = false)
    private String oldValue;

    @Column(name = "NEW_VALUE", updatable = false)
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditChange that = (AuditChange) o;
        return Objects.equals(getKey(), that.getKey()) &&
                Objects.equals(getOldValue(), that.getOldValue()) &&
                Objects.equals(getNewValue(), that.getNewValue());
    }

    @Override
    public int hashCode() {
        return hash(getKey(), getOldValue(), getNewValue());
    }
}
