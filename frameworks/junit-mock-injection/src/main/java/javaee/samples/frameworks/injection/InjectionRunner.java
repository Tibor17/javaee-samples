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

import javaee.samples.frameworks.injection.spi.InjectionPoint;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isInterface;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static java.util.ServiceLoader.load;
import static javaee.samples.frameworks.injection.BeanManager.getEmBeanType;
import static javaee.samples.frameworks.injection.DB.H2;
import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static javaee.samples.frameworks.injection.FieldUtils.filterGenericTypes;
import static javaee.samples.frameworks.injection.H2Storage.DEFAULT_STORAGE;
import static javaee.samples.frameworks.injection.JPARuleBuilder.unitName;
import static javaee.samples.frameworks.injection.Mode.DEFAULT_MODE;
import static javaee.samples.frameworks.injection.PathFinder.path;

public final class InjectionRunner extends BlockJUnit4ClassRunner {
    private static final Logger LOG = Logger.getGlobal();

    private static final class PersistenceContextWrapper {
        private boolean usedInFieldInjection = true;
        private PersistenceContext pc;
        private JPARule rule;

        private PersistenceContextWrapper(PersistenceContext pc) {
            this.pc = pc;
        }
    }

    private static final Object LOCK = new Object();

    static final ThreadLocal<PersistenceContextWrapper> PERSISTENCE_CONFIG = new ThreadLocal<>();

    private static final ThreadLocal<List<InjectionPoint>> SPI = new ThreadLocal<List<InjectionPoint>>() {
        @Override
        protected List<InjectionPoint> initialValue() {
            List<InjectionPoint> spi = new ArrayList<>();
            load(InjectionPoint.class)
                    .forEach(spi::add);
            Collections.sort(spi);
            return spi;
        }
    };

    /**
     * Creates JUnit runner to run {@code klass}.
     *
     * @param clazz test to run
     * @throws InitializationError if the test class is malformed.
     */
    public InjectionRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        PERSISTENCE_CONFIG.remove();
        fetchPersistenceConfig(method);
        Statement methodBlock = super.methodBlock(method);
        PERSISTENCE_CONFIG.remove();
        return wrapMethodBlock(methodBlock);
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        fetchPersistenceConfig(test);
        BeanManager beanManager = new BeanManager();
        injectEntityManagerProxy(beanManager, test);
        discoverExistingObjectsIfAbsent(beanManager, test);
        inject(beanManager, test.getClass(), test, path(test.getClass()));
        return test;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> rules = super.getTestRules(target);
        PersistenceContextWrapper wrapper = PERSISTENCE_CONFIG.get();
        if (!wrapper.usedInFieldInjection) {
            rules.add(wrapper.rule);
        }
        return rules;
    }

    private static Statement wrapMethodBlock(Statement methodBlock) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    methodBlock.evaluate();
                } finally {
                    SPI.get().forEach(InjectionPoint::destroy);
                    SPI.remove();
                }
            }
        };
    }

    /**
     * Search non-Inherited annotation.
     */
    private static <T extends Annotation> T findOnTest(Class<T> annotation, Object testInstance) {
        Class<?> type = testInstance.getClass();
        for (; type != null; type = type.getSuperclass()) {
            T a = type.getDeclaredAnnotation(annotation);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    private static void setPersistenceConfigIfAbsent(PersistenceContext pu) {
        PersistenceContextWrapper wrapper = PERSISTENCE_CONFIG.get();
        if (wrapper == null) {
            PERSISTENCE_CONFIG.set(new PersistenceContextWrapper(pu));
        } else if (wrapper.pc == null) {
            wrapper.pc = pu;
        }
    }

    private static void fetchPersistenceConfig(FrameworkMethod method) {
        PersistenceContext pu = method.getAnnotation(PersistenceContext.class);
        setPersistenceConfigIfAbsent(pu);
    }

    private static void fetchPersistenceConfig(Object target) {
        PersistenceContext pu = findOnTest(PersistenceContext.class, target);
        setPersistenceConfigIfAbsent(pu);
    }

    @SuppressWarnings("checkstyle:innerassignment")
    private void injectEntityManagerProxy(BeanManager beanManager, Object testInstance) throws Exception {
        if (!beanManager.containsEntityManager()) {
            boolean isNonFieldPuUsed = false;
            boolean alreadyRegistered = false;
            Class<?> discoveredType = testInstance.getClass();
            final boolean isTransactionManaged = discoveredType.isAnnotationPresent(WithManagedTransactions.class);
            beanManager.scanEntities(discoveredType.isAnnotationPresent(SearchEntities.class));
            beanManager.useManagedTransactions(isTransactionManaged);

            do {
                for (Field field : discoveredType.getDeclaredFields()) {
                    if (JPARule.class.isAssignableFrom(field.getType())) {
                        if (alreadyRegistered) {
                            throw new InitializationError(JPARule.class
                                    + " appears twice - not supported.");
                        }

                        PersistenceContext nonFieldPU = PERSISTENCE_CONFIG.get().pc;
                        isNonFieldPuUsed = injectJpaRule(field, testInstance, nonFieldPU);

                        JPARule rule = (JPARule) field.get(testInstance);
                        if (isTransactionManaged) {
                            rule.useManagedTransactions();
                        }
                        addEntityManagerInContext(beanManager, rule);
                        alreadyRegistered = true;
                    }
                }
            } while ((discoveredType = discoveredType.getSuperclass()) != null);

            PersistenceContextWrapper wrapper = PERSISTENCE_CONFIG.get();
            if (!isNonFieldPuUsed && wrapper.pc != null) {
                wrapper.usedInFieldInjection = false;
                wrapper.rule = createJPARule(wrapper.pc, testInstance);
                addEntityManagerInContext(beanManager, wrapper.rule);
            }
        }
    }

    private static void addEntityManagerInContext(BeanManager beanManager, JPARule rule) {
        rule.setBeanManager(beanManager);
        beanManager.createBean(getEmBeanType(), rule.getEntityManager());
    }

    private static boolean injectJpaRule(Field field, Object testInstance, PersistenceContext nonFieldPU) throws Exception {
        if (isPublic(field.getModifiers())) {
            synchronized (LOCK) {
                if (field.isAnnotationPresent(Inject.class) && field.get(testInstance) == null) {
                    if (field.isAnnotationPresent(PersistenceContext.class)) {
                        if (nonFieldPU != null) {
                            throw new InitializationError("Do NOT use @PersistenceContext on test method and "
                                    + "JUnit Rule " + JPARule.class.getSimpleName());
                        }
                        field.set(testInstance, createJPARule(field.getAnnotation(PersistenceContext.class), testInstance));
                    } else if (nonFieldPU == null) {
                        throw new InitializationError("Injectable field "
                                + JPARule.class.getName()
                                + " does not"
                                + " declare "
                                + PersistenceContext.class.getName()
                                + " on the field or @Test-method or class.");
                    } else {
                        field.set(testInstance, createJPARule(nonFieldPU, testInstance));
                    }
                    return true;
                }
            }
        } else {
            field.setAccessible(true);
        }
        return false;
    }

    private static void discoverExistingObjectsIfAbsent(BeanManager beanManager, Object currentBean)
            throws IllegalAccessException {
        for (Field f : currentBean.getClass().getDeclaredFields()) {
            /**
             * todo add annotations/qualfs
             * @see {@link BeanType#createBeanType(Field)}
             */
            final BeanType beanType = new BeanType(f.getType(), filterGenericTypes(f));
            if (!f.isSynthetic() && f.isAnnotationPresent(Produces.class) && !beanManager.contains(beanType)) {
                f.setAccessible(true);
                Object bean = f.get(isStatic(f.getModifiers()) ? null : currentBean);
                beanManager.createBean(beanType, bean);
            }
        }
    }

    static Collection<Field> orderFields(Field... fields) {
        List<Field> fs = asList(fields);
        fs.sort(comparingInt(InjectionRunner::fieldOrdinal));
        return fs;
    }

    private static int fieldOrdinal(Field f) {
        InjectionPointOrdinal ordinal = f.getDeclaredAnnotation(InjectionPointOrdinal.class);
        return ordinal == null ? MAX_VALUE : ordinal.value();
    }

    @SuppressWarnings("checkstyle:innerassignment")
    private void inject(BeanManager beanManager, final Class<?> unproxyType, Object currentBean, PathFinder pathFinder)
            throws Exception {
        Class<?> discoveredType = unproxyType;
        do {
            for (Field f : orderFields(discoveredType.getDeclaredFields())) {
                /**
                 * todo add annotations/qualfs
                 * @see {@link BeanType#createBeanType(Field)}
                 */
                final BeanType beanType = new BeanType(f.getType(), filterGenericTypes(f));

                if (f.isSynthetic() || isFinal(f.getModifiers())) {
                    continue;
                }

                final boolean isStat = isStatic(f.getModifiers());

                final Object target = isStat ? null : currentBean;

                f.setAccessible(true);

                if (f.get(target) != null) {
                    continue;
                }

                boolean foundSpi = false;
                for (InjectionPoint ip : SPI.get()) {
                    @SuppressWarnings("unchecked") Class<? extends Annotation> annType = ip.getAnnotationType();
                    Annotation ann = f.getAnnotation(annType);
                    if (ann == null) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Optional<Object> inject = ip.lookupOf(beanType.getType(), ann, currentBean, unproxyType);
                    foundSpi = inject.isPresent();
                    if (foundSpi) {
                        f.set(target, inject.get());
                        break;
                    }
                }

                if (!foundSpi) {
                    if (!isStat && f.isAnnotationPresent(Inject.class)) {
                        Bean<?> newBean = beanManager.getReference(beanType);
                        Object beanInstance =
                                newBean == null ? newBeanUnwrapped(beanManager, beanType, pathFinder) : newBean.getProxy();
                        f.set(currentBean, beanInstance);
                    } else if (f.isAnnotationPresent(Resource.class)) {
                        Bean<?> newBean = beanManager.getReference(beanType);
                        Object beanInstance =
                                newBean == null ? newBeanUnwrapped(beanManager, beanType, pathFinder) : newBean.getProxy();
                        f.set(target, beanInstance);
                    }
                }

            }
        } while ((discoveredType = discoveredType.getSuperclass()) != null);
    }

    private Object newBeanUnwrapped(BeanManager beanManager, BeanType beanType, PathFinder pathFinder) throws Exception {
        pathFinder = path(pathFinder, beanType.getType());
        if (!beanManager.contains(beanType)) {
            Object newBean;
            if (isAbstract(beanType.getTypeModifiers()) || isInterface(beanType.getTypeModifiers())) {
                newBean = null;
            } else {
                newBean = newInstanceWithConstructorInjection(beanManager, beanType, pathFinder);
                if (newBean == null) {
                    newBean = newInstanceWithNoArgConstructor(beanType, pathFinder);
                }
            }
            Bean<?> bean = beanManager.createBean(beanType, newBean);
            if (bean.hasDelegate()) {
                inject(beanManager, beanType.getType(), bean.getDelegate(), pathFinder);
            }
        }
        return beanManager.getReference(beanType)
                .getProxy();
    }

    private static Object newInstanceWithNoArgConstructor(BeanType beanType, PathFinder pathFinder) throws Exception {
        try {
            Constructor<?> c = beanType.getType().getDeclaredConstructor();
            int modifiers = c.getModifiers();

            // CDI documentation chapter 3.15 Unproxyable bean types:
            // http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#client_proxies
            if (isPrivate(modifiers)) {
                throw new IllegalAccessException(beanType.getType()
                        + " cannot use private and no-argument constructor");
            }

            if (c.isSynthetic()) {
                throw new IllegalAccessException(beanType.getType()
                        + " synthetic constructor is illegal to construct a bean");
            }

            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException(e.getLocalizedMessage()
                    + ": Default constructor not found."
                    + "\n"
                    + pathFinder.toString());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Exception in constructor: "
                    + e.getLocalizedMessage()
                    + "\n"
                    + pathFinder.toString());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("use private-package, protected or public constructor: "
                    + e.getLocalizedMessage()
                    + "\n"
                    + pathFinder.toString());
        } catch (ExceptionInInitializerError e) {
            throw new RuntimeException("exception in a static initializer: "
                    + e.getLocalizedMessage()
                    + "\n"
                    + pathFinder.toString());
        } catch (InstantiationException e) {
            throw new RuntimeException("exception in a initializer while creating new object: "
                    + e.getLocalizedMessage()
                    + "\n"
                    + pathFinder.toString());
        }
    }

    private Object newInstanceWithConstructorInjection(BeanManager beanManager, BeanType beanType, PathFinder pathFinder)
            throws Exception {
        for (Constructor<?> c : beanType.getType().getDeclaredConstructors()) {
            int modifiers = c.getModifiers();
            //todo observe annotation/qualifier and a type via c.getParameters()
            Class<?>[] parameters = c.getParameterTypes();
            // CDI documentation chapter 3.15 Unproxyable bean types:
            // http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#client_proxies
            if (c.isAnnotationPresent(Inject.class) && (!isPrivate(modifiers) || parameters.length != 0)) {
                Object[] args = new Object[parameters.length];
                int i = 0;
                for (Class<?> parameter : parameters) {
                    /**
                     * todo add annotations/qualfs
                     * @see {@link BeanType#createBeanType(Field)}
                     */
                    BeanType paramBeanType = new BeanType(parameter);
                    Bean<?> bean = beanManager.getReference(paramBeanType);
                    final Object p;
                    if (bean == null) {
                        p = newBeanUnwrapped(beanManager, paramBeanType, pathFinder);
                    } else {
                        p = bean.getProxy();
                    }
                    if (p == null) {
                        LOG.warning("@Inject "
                                + c
                                + " could not resolve injection point with type "
                                + parameter
                                + ". Passed NULL.");
                    }
                    args[i++] = p;
                }

                try {
                    c.setAccessible(true);
                    return c.newInstance(args);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Exception in constructor: "
                            + e.getLocalizedMessage()
                            + "\n"
                            + pathFinder.toString());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("use whatever constructor modifier except for no-parameter private: "
                            + e.getLocalizedMessage()
                            + "\n"
                            + pathFinder.toString());
                } catch (ExceptionInInitializerError e) {
                    throw new RuntimeException("exception in a static initializer: "
                            + e.getLocalizedMessage()
                            + "\n"
                            + pathFinder.toString());
                }

            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static boolean canConstruct(Class<?> type) {
        int modifiers = type.getModifiers();
        return isPublic(modifiers) && !isInterface(modifiers) && !isAbstract(modifiers);
    }

    private static JPARule createJPARule(PersistenceContext ctx, Object test) {
        DatabaseConfiguration config = findOnTest(DatabaseConfiguration.class, test);

        if (config == null) {
            return new JPARule(ctx, H2, DEFAULT_STORAGE, DEFAULT_MODE);
        }

        switch (config.value()) {
            case H2:
                return new JPARule(ctx, H2, config.h2().storage(), config.h2().mode());
            case UNDEFINED:
            default:
                Map<String, String> properties = new HashMap<>();
                for (PersistenceProperty property : ctx.properties()) {
                    properties.put(property.name(), property.value());
                }
                return unitName(ctx.unitName())
                        .database(UNDEFINED)
                        .noInternalProperties()
                        .properties(properties)
                        .build();
        }
    }
}
