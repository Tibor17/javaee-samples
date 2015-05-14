import javaee.samples.frameworks.junitjparule.JPARule;
import org.junit.Rule;
import org.junit.Test;

import static javaee.samples.frameworks.junitjparule.JPARuleBuilder.*;

public class DummyTest {
    @Rule
    public final JPARule rule = unitName("abc").build();

    @Test
    public void test() {}
}
