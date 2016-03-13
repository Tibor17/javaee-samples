package audit.domain;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "AUDIT_FLOW")
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
