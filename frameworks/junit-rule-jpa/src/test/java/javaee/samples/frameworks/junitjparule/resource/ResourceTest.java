package javaee.samples.frameworks.junitjparule.resource;

import javaee.samples.frameworks.junitjparule.InjectionRunner;
import javaee.samples.frameworks.junitjparule.WithManagedTransactions;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Session;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(InjectionRunner.class)
@WithManagedTransactions
public class ResourceTest {
    @Produces
    Session session = mock(Session.class);

    @Inject
    Session in;

    @Resource
    static Session staticResource;

    @Resource
    static Session thisResource;

    @Inject
    SessionService service;

    @Test
    public void shouldInjectResource() {
        assertThat(in)
                .isNotNull();

        assertThat(staticResource)
                .isNotNull();

        assertThat(thisResource)
                .isNotNull();

        assertThat(service)
                .isNotNull();

        assertThat(service)
                .extracting(SessionService::getSession)
                .doesNotContainNull();

        assertThat(service)
                .extracting(SessionService::getStaticResource)
                .doesNotContainNull();

        assertThat(service)
                .extracting(SessionService::getThisResource)
                .doesNotContainNull();
    }
}
