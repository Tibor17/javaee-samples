package javaee.samples.frameworks.junitjparule.resource;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Session;
import javax.transaction.Transactional;

public class SessionService {
    @Inject
    Session session;

    @Resource
    static Session staticResource;

    @Resource
    static Session thisResource;

    public Session getSession() {
        return session;
    }

    public Session getStaticResource() {
        return staticResource;
    }

    public Session getThisResource() {
        return thisResource;
    }

    @Transactional
    public void forceTransactionalProxy() {
    }
}
