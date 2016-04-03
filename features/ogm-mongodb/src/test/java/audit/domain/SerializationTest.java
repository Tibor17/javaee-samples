package audit.domain;

import org.junit.Test;

import java.io.*;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;

public class SerializationTest {
    @Test
    public void test() throws IOException, ClassNotFoundException {
        Audit expected = new Audit();
        expected.setModule("module");
        expected.setInitiator(1L);
        expected.setRequest(randomUUID());
        AuditFlow flow = new AuditFlow();
        flow.setError("error");
        expected.getFlows().add(flow);
        AuditHeader header = new AuditHeader();
        header.setKey("hk");
        header.setValue("hv");
        flow.getHeaders().add(header);
        AuditChange change = new AuditChange();
        change.setKey("ck");
        change.setOldValue("cov");
        change.setNewValue("cnv");
        flow.getChanges().add(change);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream serializer = new ObjectOutputStream(stream);
        serializer.writeObject(expected);
        serializer.flush();

        ObjectInputStream deserializer = new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray()));
        Audit actual = (Audit) deserializer.readObject();

        assertThat(actual)
                .extracting(Audit::getModule)
                .containsExactly(expected.getModule());

        assertThat(actual)
                .extracting(Audit::getInitiator)
                .containsExactly(expected.getInitiator());

        assertThat(actual)
                .extracting(Audit::getRequest)
                .containsExactly(expected.getRequest());

        assertThat(actual.getFlows())
                .extracting(AuditFlow::getError)
                .containsExactly(flow.getError());

        assertThat(actual.getFlows().get(0).getHeaders().get(0))
                .extracting(AuditHeader::getKey, AuditHeader::getValue)
                .containsSequence(header.getKey(), header.getValue());

        assertThat(actual.getFlows().get(0).getChanges().get(0))
                .extracting(AuditChange::getKey, AuditChange::getOldValue, AuditChange::getNewValue)
                .containsSequence(change.getKey(), change.getOldValue(), change.getNewValue());
    }
}
