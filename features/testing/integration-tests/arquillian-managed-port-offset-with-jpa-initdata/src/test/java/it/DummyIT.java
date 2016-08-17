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
package it;

import init.TestDataCreator;
import jpa.User;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.util.Collection;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;
// import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.*; Do NOT use due to creates very big WAR new AcceptScopesStrategy(COMPILE, IMPORT, RUNTIME, TEST)

/**
 * http://arquillian.org/guides/shrinkwrap_introduction/
 * http://stackoverflow.com/questions/13001371/adding-all-maven-dependencies-to-arquillian
 * http://serviceorientedarchitect.com/
 *
 * too old Arquillian API but maybe a hint:
 * https://developer.jboss.org/thread/237174?start=0&tstart=0
 * https://dzone.com/articles/using-shrinkwrap-maven
 */
@RunWith(Arquillian.class)
public class DummyIT {
    @PersistenceContext(unitName = "pu")
    private EntityManager em;

    @Deployment(name = "as-maven-importer", order = 1)
    @TargetsContainer("arquillian-wildfly-managed-1")
    @OverProtocol("Servlet 3.0")
    public static Archive<?> createTestArchive1() {
        /* unfortunately your test dependencies must be listed here */
        File[] assertJ = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();

        // important:: the deployment name is taken as artifact file name
        // because we have two deployments we should rename one deployment to "customized-webcontent.war"
        return create(MavenImporter.class, "customized-webcontent.war")
                .loadPomFromFile("pom.xml")
                .importBuildOutput(/*Do NOT use due to creates very big WAR new AcceptScopesStrategy(COMPILE, IMPORT, RUNTIME, TEST)*/)
                .as(WebArchive.class)
                .addAsLibraries(assertJ)
                .addClass(TestDataCreator.class)
                .addAsWebInfResource("META-INF/persistence.xml", "classes/META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new File(getProperty("web.xml")), "web.xml");
    }

    @Deployment(name = "as-maven-artifact-file", order = 2)
    @TargetsContainer("arquillian-wildfly-managed-2")
    @OverProtocol("Servlet 3.0")
    public static Archive<?> createTestArchive2() {
        /* unfortunately your test dependencies must be listed here */
        File[] assertJ = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();

        // important:: the deployment name is taken as artifact file name
        return createFromZipFile(WebArchive.class, new File(getProperty("artifact.file")))
                .addAsLibraries(assertJ)
                .addClass(TestDataCreator.class);
    }

    @Test
    @OperateOnDeployment("as-maven-importer")
    public void shouldHaveInitDataAsMavenImporter() {
        testShouldHaveInitData();
    }

    @Test
    @OperateOnDeployment("as-maven-artifact-file")
    public void shouldHaveInitDataAsMavenArtifactFile() {
        testShouldHaveInitData();
    }

    private void testShouldHaveInitData() {
        Collection<User> users = em.createQuery("select e from User e", User.class)
                .getResultList();

        assertThat(users, is(not(empty())));

        assertThat(users, hasSize(1));

        User user = users.iterator().next();

        assertThat(user)
                .isNotNull();

        assertThat(user)
                .extracting(User::getId)
                .isNotNull();

        assertThat(user.getId())
                .isGreaterThan(0);

        assertThat(user)
                .extracting(User::getLogin, User::getPassword)
                .containsOnly("jsmith", "pswd");
    }
}
