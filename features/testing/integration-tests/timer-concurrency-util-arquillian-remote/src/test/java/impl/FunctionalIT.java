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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;

import static java.lang.System.getProperty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class FunctionalIT {
    private static final Logger LOG = Logger.getGlobal();

    @PersistenceContext(unitName = "pu")
    private EntityManager em;

    @Deployment(name = "FunctionalIT-shouldFindTimerTicks")
    @OverProtocol("Servlet 3.0")
    public static Archive<?> createTestArchive() {
        return create(MavenImporter.class)
                .loadPomFromFile("pom.xml")
                .importBuildOutput()
                .as(WebArchive.class)
                .addAsWebInfResource("META-INF/persistence.xml", "classes/META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new File(getProperty("web.xml")), "web.xml")
                .addAsResource(new StringAsset("Manifest-Version: 1.0\r\n" +
                                "Dependencies: org.apache.commons.collections export, org.apache.commons.lang export"),
                        "META-INF/MANIFEST.MF");
    }

    @Test
    @OperateOnDeployment("FunctionalIT-shouldFindTimerTicks")
    public void shouldFindTimerTicks(@ArquillianResource @OperateOnDeployment("FunctionalIT-shouldFindTimerTicks")
                                         URL deployment)
            throws InterruptedException {

        LOG.info(deployment.toExternalForm());

        MILLISECONDS.sleep(1500);

        Collection<MyEntity> results =
                em.createQuery("select e from MyEntity e", MyEntity.class)
                        .getResultList();

        assertThat(results, hasSize(greaterThan(0)));
    }
}
