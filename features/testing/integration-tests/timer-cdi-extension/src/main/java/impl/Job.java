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
package impl;

import jpa.MyEntity;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;

/**
 * https://docs.oracle.com/cd/E19857-01/820-1639/bhano/index.html
 * https://docs.jboss.org/jbossas/docs/Server_Configuration_Guide/4/html/ENC_Usage_Conventions-Resource_Manager_Connection_Factory_References_with_jboss.xml_and_jboss_web.xml.html
 * https://docs.jboss.org/jbossweb/3.0.x/jndi-resources-howto.html
 */
@ApplicationScoped
public class Job implements Runnable {
    @PersistenceContext(unitName = "pu")
    EntityManager em;

    @Resource(lookup = "java:comp/env")
    Context ctx;

    @Resource(lookup = "java:comp/env/mail/Session")
    Session session;

    @Resource(lookup = "java:comp/env/mail/Session/Notifier")
    Session sessionNotifier;

    @Transactional
    public void run() {
        err.println("time process " + currentTimeMillis());
        em.persist(new MyEntity());

        try {
            Session mail = (Session) ctx.lookup("mail/Session/Notifier");
            assert "email@gmail.com".equals(mail.getProperty("mail.from"));
            assert "email@gmail.com".equals(session.getProperty("mail.from"));
            assert "email@gmail.com".equals(sessionNotifier.getProperty("mail.from"));
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
