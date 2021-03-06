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
package javaee.samples.frameworks.injection;

import javaee.samples.frameworks.tsupporth2.H2Utils;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceProperty;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.transaction.TransactionalException;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javaee.samples.frameworks.injection.DB.H2;
import static javaee.samples.frameworks.injection.DBLock.NO;
import static javaee.samples.frameworks.injection.DBLock.SOCKET;
import static javax.ejb.TransactionAttributeType.MANDATORY;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;
import static javax.persistence.Persistence.createEntityManagerFactory;

public final class JPARule extends TestWatcher {
    private static final Logger LOG = Logger.getGlobal();

    static final ThreadLocal<JPARule> CURRENT_JPA_RULE = new InheritableThreadLocal<>();

    private static final String H2_STORAGE_PATH = "./target/h2/";
    private static final Class<?>[] EMF = {EntityManagerFactory.class};
    private static final Class<?>[] EM = {SynchronizedEntityManager.class};
    private static final String USER = "sa";
    private static final String PASS = "";
    private static final String URL_PREFIX = "jdbc:h2:file:";
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final Queue<Class<?>> preferableDomains = new ConcurrentLinkedQueue<>();
    private final Collection<EntityManager> entityManagers = new ArrayList<>();
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    private final String unitName;
    private final H2Storage storage;
    private final Mode mode;

    private volatile BeanManager beanManager = new BeanManager();
    private volatile EntityManagerFactory factoryProxy;
    private volatile String dbPath;

    private boolean useAutoServerMode;

    /**
     * When using this feature, by default the server uses any free TCP port.
     * The port can be set manually using AUTO_SERVER_PORT=9090.
     */
    private boolean closeSessionOnExitJVM;

    private boolean useProperties = true;

    /**
     * H2 command "SET DB_CLOSE_DELAY <seconds>".
     */
    private int closeDbDelayInSeconds = -1;

    private DBLock lock = NO;

    private DB db;

    private volatile boolean useManagedTransactions;

    JPARule(JPARuleBuilder builder) {
        properties.putAll(builder.getProperties());
        unitName = builder.getUnitName();
        storage = builder.getStorage();
        mode = builder.getMode();
        useAutoServerMode = builder.isUseAutoServerMode();
        closeDbDelayInSeconds = builder.getCloseDbDelayInSeconds();
        closeSessionOnExitJVM = builder.isCloseSessionOnExitJVM();
        useProperties = builder.isUseProperties();
        db = builder.getDb();
    }

    JPARule(PersistenceContext unit, DB db, H2Storage storage, Mode mode) {
        for (PersistenceProperty property : unit.properties()) {
            properties.put(property.name(), property.value());
        }
        unitName = unit.unitName();
        this.storage = storage;
        this.mode = mode;
        this.db = db;
    }

    public String getUnitName() {
        return unitName;
    }

    void setBeanManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    /**
     * use annotation {@link WithManagedTransactions} on the same level as RunWith.
     */
    void useManagedTransactions() {
        useManagedTransactions = true;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return factoryProxy;
    }

    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    protected EntityManager getCurrentEntityManager() {
        return beanManager.getCurrentEntityManager();
    }

    public EntityManager getEntityManager() {
        return (EntityManager) newProxyInstance(context(), EM, (proxy, method, args) -> {
            EntityManager em = JPARule.this.getCurrentEntityManager();
            try {
                switch (method.getName()) {
                    case "getTransaction":
                        if (JPARule.this.useManagedTransactions) {
                            throw new IllegalStateException("Method EntityManager.getTransaction() cannot be"
                                    + " called with managed transaction.");
                        }
                    case "unwrapTransaction":
                        return em.getTransaction();
                    case "close":
                        throw new IllegalStateException("the entity manager is managed");
                    case "closeSafely":
                        boolean isOpen = em.isOpen();
                        if (isOpen) {
                            em.close();
                        }
                        return isOpen;
                    default:
                        return method.invoke(em, args);
                }
            } catch (PersistenceException e) {
                String msg = e.getLocalizedMessage();
                if (!preferableDomains.isEmpty() && msg != null && msg.contains("Unable to build Hibernate SessionFactory")) {
                    Throwable t = e.getCause();
                    if (t != null) {
                        msg = t.getLocalizedMessage();
                        if (msg != null && msg.contains("references an unknown entity:")) {
                            printPersistenceXmlEntities();
                        }
                    }
                }
                throw e;
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (t == null) {
                    t = e;
                }

                if (!preferableDomains.isEmpty()) {
                    String msg = t.getLocalizedMessage();
                    if (msg != null && msg.contains("is not mapped")) {
                        printPersistenceXmlEntities();
                    }
                }
                throw t;
            }
        });
    }

    @Override
    protected void starting(Description description) {
        CURRENT_JPA_RULE.set(this);

        String desc = buildDatabaseFileName(description);

        if (db == H2) {
            dbPath = createDbPath(desc);
            deleteFile(dbPath);
        }

        Map<String, String> emfProperties = new HashMap<>();
        if (useProperties) {
            if (db == H2) {
                String h2Storage = resolveH2Storage();
                String h2Lock = lock == null ? "" : ";FILE_LOCK=" + (useAutoServerMode ? SOCKET : this.lock).name();
                String closer = format(";AUTO_SERVER=%s;DB_CLOSE_ON_EXIT=%s;DB_CLOSE_DELAY=%d",
                        asString(useAutoServerMode), asString(closeSessionOnExitJVM), closeDbDelayInSeconds);

                emfProperties.putIfAbsent("javax.persistence.jdbc.driver", "org.h2.Driver");
                emfProperties.putIfAbsent("javax.persistence.jdbc.url", URL_PREFIX + dbPath
                                // Don't use MULTI_THREADED: it's experimental and old configuration + ";MULTI_THREADED=1"
                                + (h2Storage == null ? "" : ";" + h2Storage)
                                + h2Lock // ;FILE_LOCK=NO does not work together with AUTO_SERVER
                                + closer // DB_CLOSE_ON_EXIT=FALSE breaks the commits
                                + ";LOCK_TIMEOUT=60000" // in millis
                                + ";QUERY_TIMEOUT=0" // in millis, default:0 means no-timeout
                                + ";EARLY_FILTER=TRUE" // performance increase in 53%
                                + ";PAGE_SIZE=2048" // in bytes, default:2048
                                + ";CACHE_SIZE=256" // in KB
                                + ";CACHE_TYPE=SOFT_LRU"
                                + ";MAX_MEMORY_ROWS=16384"
                                + databaseMode()
                        /**
                         * workaround: https://groups.google.com/forum/#!topic/h2-database/xt8BW5fp1eI
                         */
                        //+ ";MAX_MEMORY_ROWS_DISTINCT =16384"
                );
                // How to disable Experimental MV Storage - concatenate with string ";MV_STORE=FALSE;MVCC=FALSE"
                // http://stackoverflow.com/questions/23806471/why-is-my-embedded-h2-program-writing-to-a-mv-db-file
                // See the H2 RELEASE_NOTES http://www.h2database.com/html/changelog.html
                // See the H2 performance tuning http://www.iliachemodanov.ru/en/blog-en/21-databases/42-h2-performance-en

                emfProperties.putIfAbsent("javax.persistence.jdbc.user", USER);
                emfProperties.putIfAbsent("javax.persistence.jdbc.password", PASS);
                emfProperties.putIfAbsent("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            }

//    Unable to build EMF throwing javax.naming.NoInitialContextException if jtaDataSource is used
//    properties.putIfAbsent("javax.persistence.jtaDataSource", "");
            // RESOURCE_LOCAL doesn't have to be used, as it is defined in persistence.xml
            emfProperties.putIfAbsent("javax.persistence.transactionType", "RESOURCE_LOCAL");
            emfProperties.putIfAbsent("javax.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");
            emfProperties.putIfAbsent("hibernate.current_session_context_class", "thread");
            emfProperties.putIfAbsent("hibernate.cache.use_second_level_cache", "false");

            // To scan entities in classpat use "class" or "hbm" or both "class,hbm".
            // To disable the scan and accept only those entities listed in persistence.xml use other string, e.g. "false"
            emfProperties.putIfAbsent("hibernate.archive.autodetection", beanManager.scanEntities() ? "class" : "false");

            // org.jboss.jbossts:jbossjta:4.9.0.GA not necessary, otherwise use JBossStandAloneJtaPlatform for arjuna JTA
            /**
             * properties.putIfAbsent("hibernate.transaction.jta.platform",
             * "org.hibernate.service.jta.platform.internal.JBossAppServerJtaPlatform");
             */
            // JdbcTransactionFactory on RESOURCE_LOCAl, otherwise JtaTransactionFactory on JTA
            emfProperties.putIfAbsent("hibernate.transaction.factory_class",
                    "org.hibernate.engine.transaction.internal.jdbc.JdbcTransactionFactory");
            // If the entity manager factory name is already registered, defaults to "org.jbpm.persistence.jpa".
            /**
             * If entity manager will be clustered or passivated, specify a unique value for property
             * 'hibernate.ejb.entitymanager_factory_name'
             */
            emfProperties.putIfAbsent("hibernate.ejb.entitymanager_factory_name", "EMF_" + desc);
            emfProperties.putIfAbsent("hibernate.hbm2ddl.auto", "create");
            emfProperties.putIfAbsent("hibernate.show_sql", "true");
            emfProperties.putIfAbsent("hibernate.format_sql", "true");
        }
        emfProperties.putAll(properties);

        final EntityManagerFactory factory = createEntityManagerFactory(unitName, emfProperties);

        factoryProxy = (EntityManagerFactory) newProxyInstance(context(), EMF, (proxy, method, args) -> {
            try {
                Object result = method.invoke(factory, args);
                if ("createEntityManager".equals(method.getName())) {
                    entityManagers.add((EntityManager) result);
                }
                return result;
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (t == null) {
                    t = e;
                }
                throw t;
            }
        });

        beanManager.setEntityManagerFactory(getEntityManagerFactory(), emfProperties);

        if (description.getAnnotation(Transactional.class) != null) {
            getCurrentEntityManager().getTransaction().begin();
        }
    }

    public void $(final Consumer<EntityManager> block) {
        $$(e -> {
            block.accept(e);
            return null;
        });
    }

    public <T> T $(final Supplier<T> block) {
        return $$(e -> block.get());
    }

    @SuppressWarnings("unused")
    public void $(final Runnable block) {
        $$(e -> {
            block.run();
            return null;
        });
    }

    public <T> T $$(Function<EntityManager, T> block) {
        if (beanManager.isEntityManagerExistsOpen()) {
            beanManager.getCurrentEntityManager().close();
        }
        final EntityManager em = beanManager.getCurrentEntityManager();
        final EntityTransaction transaction = em.getTransaction();
        if (transaction.isActive()) {
            throw new TransactionalException("Transactional block must not be called within current transaction.",
                    new InvalidTransactionException("transaction in " + Thread.currentThread()));
        }

        Optional<Throwable> t = empty();

        try {
            transaction.begin();
            return block.apply(em);
        } catch (Throwable e) {
            t = of(e);
            e.printStackTrace();
        } finally {
            try {
                if (t.isPresent()) {
                    t = performAndAddSuppressedException(transaction::rollback, t);
                } else {
                    t = perform(transaction::commit);
                    t.ifPresent(Throwable::printStackTrace);
                }
            } finally {
                boolean noExceptionBefore = !t.isPresent();
                t = performAndAddSuppressedException(em::close, t);
                t.filter(x -> noExceptionBefore).ifPresent(Throwable::printStackTrace);
            }

            t.ifPresent(x -> { throw new TransactionalException(x.getLocalizedMessage(), x); });
        }

        throw new AssertionError("unreachable statement");
    }

    @Override
    protected void finished(Description description) {
        CURRENT_JPA_RULE.remove();
        beanManager.clear();
        TransactionManager.clean();
        try {
            if (db == H2) {
                LOG.info("H2 database appears in " + new File(dbPath).getCanonicalPath());
            }
            entityManagers.stream()
                    .filter(EntityManager::isOpen)
                    .forEach(EntityManager::close);
            entityManagers.clear();

            if (factoryProxy != null && factoryProxy.isOpen()) {
                factoryProxy.close();
            }
        } catch (PersistenceException | IOException e) {
            e.printStackTrace();
        } finally {
            if (db == H2) {
                try {
                    // Don't use native H2 SHUTDOWN, it fails in JTA.
                    // http://www.h2database.com/html/grammar.html#shutdown
                    H2Utils.shutdownH2(URL_PREFIX + dbPath, USER, PASS);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (isTransactionalTest(description) && canRollback(e, description)) {
            EntityManager em = getCurrentEntityManager();
            EntityTransaction et = em.getTransaction();
            if (et.isActive()) {
                et.rollback();
            }

            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        if (isTransactionalTest(description)) {
            EntityManager em = getCurrentEntityManager();
            EntityTransaction et = em.getTransaction();
            if (et.isActive()) {
                et.commit();
            }

            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    protected void succeeded(Description description) {
        if (isTransactionalTest(description)) {
            EntityManager em = getCurrentEntityManager();
            EntityTransaction et = em.getTransaction();
            if (et.isActive()) {
                et.commit();
            }

            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean execute(String command) throws SQLException, IOException {
        if (db != H2) {
            throw new IllegalStateException("can be called only for H2 database");
        }
        return H2Utils.execute(dbPath, USER, PASS, command);
    }

    public void deleteRowsFromTables() throws IOException, SQLException {
        if (db != H2) {
            throw new IllegalStateException("can be called only for H2 database");
        }
        H2Utils.deleteRowsFromTables(dbPath, USER, PASS);
    }

    public void dropAllObjects() throws IOException, SQLException {
        if (db != H2) {
            throw new IllegalStateException("can be called only for H2 database");
        }
        H2Utils.dropAllObjects(dbPath);
    }

    private void printPersistenceXmlEntities() {
        System.err.println("*************************** Expected Classes in persistence.xml ***************************");
        DomainUtils.printPersistenceXmlEntities(System.err, preferableDomains);
        System.err.println("*******************************************************************************************");
    }

    private String resolveH2Storage() {
        if (storage == null) {
            return null;
        } else {
            switch (storage) {
                case ENABLE_MV_STORE:
                    return "MV_STORE=TRUE";
                case DISABLE_MV_STORE:
                    return "MV_STORE=FALSE";
                case ENABLE_MVCC:
                    return "MVCC=TRUE";
                case MULTI_THREADED_1:
                    return "MULTI_THREADED=1";
                case DEFAULT_STORAGE:
                default:
                    return null;
            }
        }
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

    @SuppressWarnings("all")
    private static void deleteFile(String f) {
        try {
            File file = new File(f);
            file = file.getCanonicalFile();
            if (file.isFile()) {
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String databaseMode() {
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

    @SuppressWarnings("all")
    private static String createDbPath(String relPath) {
        new File(H2_STORAGE_PATH).mkdirs();
        String dbPath;
        do {
            dbPath = H2_STORAGE_PATH + relPath + "_" + COUNTER.getAndIncrement();
        } while (new File(dbPath + ".h2.db").exists() || new File(dbPath + ".mv.db").exists());

        return dbPath;
    }

    private static String asString(boolean b) {
        return Boolean.toString(b).toUpperCase(Locale.ENGLISH);
    }

    private static ClassLoader context() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static Optional<Throwable> perform(Runnable r) {
        return performAndAddSuppressedException(r, empty());
    }

    private static Optional<Throwable> performAndAddSuppressedException(@NotNull Runnable r, @NotNull Optional<Throwable> t) {
        try {
            r.run();
            return t;
        } catch (IllegalStateException | RollbackException e) {
            t.ifPresent(x -> x.addSuppressed(e));
            return of(t.orElse(e));
        }
    }

    private static boolean isClassAssignableTo(Class<?> from, Class<?>... to) {
        for (Class<?> clazz : to) {
            if (clazz.isAssignableFrom(from)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTransactionalTest(Description description) {
        Transactional transactional = description.getAnnotation(Transactional.class);
        if (transactional != null) {
            TxType type = transactional.value();
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

    static boolean canRollback(Throwable e, Description description) {
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
}
