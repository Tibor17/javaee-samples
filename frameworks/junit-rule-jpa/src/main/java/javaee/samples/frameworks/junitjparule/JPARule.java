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
package javaee.samples.frameworks.junitjparule;

import javaee.samples.frameworks.tcontext.TransactionalEntityManager;
import javaee.samples.frameworks.tcontext.TransactionalState;
import javaee.samples.frameworks.tcontext.UnitEntityManager;
import javaee.samples.frameworks.tsupporth2.H2Utils;
import org.hamcrest.Matcher;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.transaction.TransactionalException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static javax.ejb.TransactionAttributeType.*;
import static javaee.samples.frameworks.tsupporth2.H2Utils.shutdownH2;

public final class JPARule extends TestWatcher {
    private static final String USER = "sa";
    private static final String PASS = "";
    private static final String URL_PREFIX = "jdbc:h2:";
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Map<PersistenceKey, ThreadLocal<UnitEntityManager>> currentEntityManagers
            = new ConcurrentHashMap<>();

    final Map<String, String> properties = new HashMap<>();
    private final Queue<EntityManager> entityManagers = new ConcurrentLinkedQueue<>();
    private final String unitName;
    private final boolean transactional;
    private final boolean doNotCommitOwnTransaction;
    private final boolean joinTransaction;
    private final H2Storage storage;
    private final Mode mode;
    private volatile EntityManagerFactory factory;
    private volatile File dbPath;

    JPARule(JPARuleBuilder builder) {
        properties.putAll(builder.properties);
        this.unitName = builder.unitName;
        this.transactional = builder.transactional;
        this.doNotCommitOwnTransaction = builder.doNotCommitOwnTransaction;
        this.joinTransaction = builder.joinTransaction;
        this.storage = builder.storage;
        this.mode = builder.mode;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return factory;
    }

    public EntityManager createEntityManager() {
        EntityManagerFactory emf = getEntityManagerFactory();
        return emf == null ? null : emf.createEntityManager();
    }

    public EntityManager currentEntityManager() {
        InvocationHandler handler = (Object o, Method m, Object[] a) -> {
            if (JPARule.this.transactional && "getTransaction".equals(m.getName())) {
                throw new IllegalStateException("The transaction is managed.");
            }
            UnitEntityManager em = JPARule.this.getCurrentEM();
            /*if (JPARule.this.transactional && em != null) {
                TransactionalEntityManager tem = (TransactionalEntityManager) em;
                if (!tem.hasOwnTransaction())//todo continue in mockito interceptor
            }*/
            try {
                return em == null ? null : m.invoke(em, a);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        };
        final Class<EntityManager> c = EntityManager.class;
        return c.cast(Proxy.newProxyInstance(getClassLoader(c), new Class<?>[]{c}, handler));
    }

    @Override
    protected void starting(Description description) {
        final String desc = buildDatabaseFileName(description);
        dbPath = dbPath(desc);
        if (getLocalEM() != null) {
            fail("Such @Rule already defined {unitName=" + unitName + ", dbPath=" + dbPath + ", transactional=" + transactional + "}.");
        }
        shutdownAndDeleteDatabase(dbPath);
        final Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        final String storage = resolveH2Storage();
        properties.put("javax.persistence.jdbc.url", URL_PREFIX + dbPath
                        // Don't use MULTI_THREADED: it's experimental and old configuration + ";MULTI_THREADED=1"
                        + (storage == null ? "" : ";" + storage)
                        //;FILE_LOCK=NO does not work together with AUTO_SERVER
                        + ";AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=-1"//DB_CLOSE_ON_EXIT=FALSE breaks the commits
                        + ";LOCK_TIMEOUT=60000" // in millis
                        + ";QUERY_TIMEOUT=0" // in millis, default:0 means no-timeout
                        + ";EARLY_FILTER=TRUE" // performance increase in 53%
                        + ";PAGE_SIZE=2048" // in bytes, default:2048
                        + ";CACHE_SIZE=256" // in KB
                        + ";CACHE_TYPE=SOFT_LRU"
                        + ";MAX_MEMORY_ROWS=16384"
                        + concatenateWithMode()
                //+ ";MAX_MEMORY_ROWS_DISTINCT =16384" // workaround: https://groups.google.com/forum/#!topic/h2-database/xt8BW5fp1eI
        );
        // How to disable Experimental MV Storage - concatenate with string ";MV_STORE=FALSE;MVCC=FALSE"
        // http://stackoverflow.com/questions/23806471/why-is-my-embedded-h2-program-writing-to-a-mv-db-file
        // See the H2 RELEASE_NOTES http://www.h2database.com/html/changelog.html
        // See the H2 performance tuning http://www.iliachemodanov.ru/en/blog-en/21-databases/42-h2-performance-en

        properties.put("javax.persistence.jdbc.user", USER);
        properties.put("javax.persistence.jdbc.password", PASS);
//    Unable to build EMF throwing javax.naming.NoInitialContextException if jtaDataSource is used
//    properties.put("javax.persistence.jtaDataSource", "");
        properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL"); //doesn't have to be used, as it is defined in persistence.xml
        properties.put("javax.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.current_session_context_class", "thread");
        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.archive.autodetection", "class");
        // org.jboss.jbossts:jbossjta:4.9.0.GA not necessary, otherwise use JBossStandAloneJtaPlatform for arjuna JTA
        // properties.put("hibernate.transaction.jta.platform", "org.hibernate.service.jta.platform.internal.JBossAppServerJtaPlatform");
        // JdbcTransactionFactory on RESOURCE_LOCAl, otherwise JtaTransactionFactory on JTA
        properties.put("hibernate.transaction.factory_class", "org.hibernate.engine.transaction.internal.jdbc.JdbcTransactionFactory");
        // If the entity manager factory name is already registered, defaults to "org.jbpm.persistence.jpa".
        // If entity manager will be clustered or passivated, specify a unique value for property 'hibernate.ejb.entitymanager_factory_name'
        properties.put("hibernate.ejb.entitymanager_factory_name", "EMF_" + desc);
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.putAll(this.properties);
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(JPARule.this.unitName, properties);

        InvocationHandler handler = (Object o, Method m, Object[] a) -> {
            try {
                Object ret = m.invoke(emf, a);
                if ("createEntityManager".equals(m.getName())) {
                    JPARule.this.entityManagers.add((EntityManager) ret);
                }
                return ret;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        };
        Class<EntityManagerFactory> c = EntityManagerFactory.class;
        factory = c.cast(Proxy.newProxyInstance(getClassLoader(c), new Class<?>[]{c}, handler));

        if (isTransactionalTest(description)) {
            EntityTransaction et = getCurrentEM().getTransaction();
            if (doNotCommitOwnTransaction) {
                et.setRollbackOnly();
            }
            et.begin();
        }
    }

    @Override
    protected void finished(Description description) {
        RuntimeException e = null;
        try {
            entityManagers.stream()
                    .filter(EntityManager::isOpen)
                    .forEach(em -> {
                        try {
                            em.close();
                        } catch (IllegalStateException e3) {
                            e3.printStackTrace();
                        }
                    });

            if (factory != null && factory.isOpen()) {
                factory.close();
            }
        } catch (IllegalStateException e1) {
            e = e1;
        } finally {
            try {
                // Don't use native H2 SHUTDOWN, it fails in JTA.
                // http://www.h2database.com/html/grammar.html#shutdown
                shutdownH2(dbPath, USER, PASS);
            } catch (SQLException | IOException e2) {
                if (e == null) {
                    e = new RuntimeException(e2.getLocalizedMessage(), e2);
                } else {
                    e.addSuppressed(e2);
                }
            }
        }

        if (e != null) {
            throw e;
        }
    }

    @Override
    protected final void failed(Throwable e, Description description) {
        if (isTransactionalTest(description) && canRollback(e, description)) {
            EntityTransaction et = getCurrentEM().getTransaction();
            if (et.isActive()) {
                et.rollback();
            }
        }
    }

    @Override
    protected final void skipped(AssumptionViolatedException e, Description description) {
        if (transactional && !doNotCommitOwnTransaction && isTransactionalTest(description)) {
            EntityTransaction et = getCurrentEM().getTransaction();
            if (et.isActive()) {
                et.commit();
            }
        }
    }

    @Override
    protected final void succeeded(Description description) {
        if (transactional && !doNotCommitOwnTransaction && isTransactionalTest(description)) {
            EntityTransaction et = getCurrentEM().getTransaction();
            if (et.isActive()) {
                et.commit();
            }
        }
    }

    public boolean execute(String command) throws SQLException {
        return H2Utils.execute(URL_PREFIX + dbPath, USER, PASS, command);
    }

    public <T> T $(final Commitable<T> blok) {
        return $(e -> {
            return blok.commit();
        });
    }

    public <T> T $(Callable<T> block, boolean close) {
        return $(block, nullValue(Throwable.class), close);
    }

    public <T> T $(Callable<T> block) {
        return $(block, nullValue(Throwable.class), true);
    }

    public <T> T $(Callable<T> block, Matcher<Throwable> exception, boolean close) {
        final UnitEntityManager em = getCurrentEM();
        final EntityTransaction transaction = em.getTransaction();
        if (transaction.isActive()) {
            throw new TransactionalException("Transactional block must not be called within current transaction.",
                    new InvalidTransactionException("transaction in " + Thread.currentThread()));
        }

        if (doNotCommitOwnTransaction) {
            transaction.setRollbackOnly();
        }

        Throwable t = null;

        try {
            transaction.begin();
            return block.call(em);
        } catch (InvocationTargetException e) {
            t = e.getCause();
            t.printStackTrace();
        } catch (Throwable e) {
            t = e;
            t.printStackTrace();
        } finally {
            if (transaction.isActive()) {
                if (t == null) {
                    transaction.commit();
                } else {
                    transaction.rollback();
                }
            }

            if (close && em.isOpen()) {
                em.close();
            }

            assertThat(t, exception);
        }

        throw new AssertionError("unreachable statement");
    }

    private boolean isTransactionalTest(Description description) {
        Transactional transactional = description.getAnnotation(Transactional.class);
        if (transactional != null) {
            Transactional.TxType type = transactional.value();
            if (type == TxType.REQUIRED || type == TxType.REQUIRES_NEW) {
                return true;
            }
            if (type == TxType.MANDATORY) {
                String msg = "test called outside a transaction context";
                throw new TransactionalException(msg, new TransactionRequiredException(msg));
            }
        }
        TransactionAttribute transactionAttribute = description.getAnnotation(TransactionAttribute.class);
        if (transactionAttribute != null) {
            TransactionAttributeType type = transactionAttribute.value();
            if (type == REQUIRED || type == REQUIRES_NEW) {
                return true;
            }
            if (type == MANDATORY) {
                String msg = "test called outside a transaction context";
                throw new TransactionalException(msg, new TransactionRequiredException(msg));
            }
        }
        return false;
    }

    private boolean canRollback(Throwable e, Description description) {
        Transactional transactional = description.getAnnotation(Transactional.class);
        if (transactional == null) {
            return true;
        } else {
            Class<?> exc = e.getClass();
            Class[] r = transactional.rollbackOn();
            Class[] d = transactional.dontRollbackOn();
            boolean rollbackOn = isClassAssignableTo(exc, r);
            boolean dontRollbackOn = isClassAssignableTo(exc, d);
            return !dontRollbackOn && (r.length == 0 || rollbackOn);
        }
    }

    private ThreadLocal<UnitEntityManager> getLocalEM() {
        return currentEntityManagers.get(new PersistenceKey(unitName, dbPath, transactional));
    }

    private UnitEntityManager getCurrentEM() {
        PersistenceKey key = new PersistenceKey(unitName, dbPath, transactional);
        ThreadLocal<UnitEntityManager> local = currentEntityManagers.get(key);
        if (local == null) {
            local = newLocalEntityManager(key);
            ThreadLocal<UnitEntityManager> previous = currentEntityManagers.putIfAbsent(key, local);
            if (previous != null)
                local = previous;
        }
        return local.get();
    }

    private String resolveH2Storage() {
        if (storage == null) {
            return null;
        } else {
            switch (storage) {
                case ENABLE_MV_STORE: return "MV_STORE=TRUE";
                case DISABLE_MV_STORE: return "MV_STORE=FALSE";
                case ENABLE_MVCC: return "MVCC=TRUE";
                case MULTI_THREADED_1: return "MULTI_THREADED=1";
                case DEFAULT_STORAGE:
                default: return null;
            }
        }
    }

    private ThreadLocal<UnitEntityManager> newLocalEntityManager(final PersistenceKey key) {
        return new ThreadLocal<UnitEntityManager>() {
            @Override
            protected UnitEntityManager initialValue() {
                final Class<? extends UnitEntityManager> c =
                        key.isTransactional() ? TransactionalEntityManager.class : UnitEntityManager.class;
                final EntityManager em = JPARule.this.factory.createEntityManager();
                final TransactionalStateImpl state = new TransactionalStateImpl();
                InvocationHandler handler = (Object o, Method m, Object[] a) -> {
                    try {
                        if ("getUnitName".equals(m.getName())) {
                            return key.getUnitName();
                        } else if ("getDatabaseStorage".equals(m.getName())) {
                            return key.getDatabaseStorage();
                        } else if (m.getDeclaringClass() == TransactionalState.class) {
                            return m.invoke(state, a);
                        } else if ("getTransaction".equals(m.getName())) {
                            EntityTransaction et = em.getTransaction();
                            if (JPARule.this.doNotCommitOwnTransaction) {
                                et.setRollbackOnly();
                            }
                            return et;
                        } else {
                            return m.invoke(em, a);
                        }
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                };
                return c.cast(Proxy.newProxyInstance(getClassLoader(c), new Class<?>[] {c}, handler));
            }

            @Override
            public UnitEntityManager get() {
                UnitEntityManager em = super.get();
                if (!em.isOpen()) {
                    em = initialValue();
                    super.set(em);
                }
                return em;
            }
        };
    }

    private static boolean isClassAssignableTo(Class<?> from, Class<?>... to) {
        for (Class<?> clazz : to) {
            if (clazz.isAssignableFrom(from)) {
                return true;
            }
        }
        return false;
    }

    private static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = clazz.getClassLoader();
        }
        return cl;
    }

    static String buildDatabaseFileName(Description description) {
        String method = description.getMethodName();
        int index = method.indexOf("[");
        if (index != -1) {
            method = method.substring(0, index);
        }
        String desc = description.getClassName()
                .trim()
                .replaceAll("\\.", "_");
        desc +=  "__";
        desc += method;
        sanityCheckMethodName(desc);
        return desc.replaceAll(" ", "")
                .replaceAll("\\$", "_");
    }

    static void sanityCheckMethodName(String methodName) {
        if (!Pattern.matches("^[\\w _\\$]+$", methodName)) {
            throw new IllegalArgumentException("Cannot create file name for DB file - wrong class/method name!");
        }
    }

    private void shutdownAndDeleteDatabase(File f) {
        if (f.isFile()) {
            try {
                shutdownH2(dbPath, USER, PASS);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                f.delete();
            }
        }
    }

    private static File dbPath(String desc) {
        try {
            return new File("./target/h2/", desc + "_" + COUNTER.getAndIncrement())
                    .getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String concatenateWithMode() {
        if (mode == null) {
            return "";
        } else {
            switch (mode) {
                case DEFAULT_MODE:
                    return "";
                default:
                    return ";MODE=" + mode.getModeAsString();
            }
        }
    }
}
