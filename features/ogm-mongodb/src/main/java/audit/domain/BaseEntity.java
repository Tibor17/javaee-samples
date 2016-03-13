package audit.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@Access(FIELD)
public abstract class BaseEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "uuid", strategy = IDENTITY)
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    public String getId() {
        return id;
    }
}
