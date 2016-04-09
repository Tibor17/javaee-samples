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

import javaee.samples.frameworks.junitjparule.spi.ContextInjector;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.enterprise.inject.Typed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.Transactional;
import javax.transaction.TransactionalException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import static javaee.samples.frameworks.junitjparule.BeanUtils.*;
import static javaee.samples.frameworks.junitjparule.TransactionManager.*;
import static java.lang.reflect.Modifier.isFinal;
import static javax.transaction.Transactional.TxType;

final class BeanManager {
    private static final ServiceLoader<ContextInjector> CONTEXT = ServiceLoader.load(ContextInjector.class);
    private static final LocalEntityManager CURRENT_ENTITY_MANAGER = new LocalEntityManager();
    private static final BeanType EM_BEAN_TYPE = new BeanType(EntityManager.class);

    private final Map<BeanType, Bean<?>> injectionPoints = new ConcurrentHashMap<>();

    private volatile EntityManagerFactory entityManagerFactory;
    private volatile Map<String, String> emProps;
    private volatile boolean useManagedTransactions;
    private volatile boolean scanEntities;

    boolean hasManagedTransactions() {
        return useManagedTransactions;
    }

    void useManagedTransactions(boolean useManagedTransactions) {
        this.useManagedTransactions = useManagedTransactions;
    }

    void scanEntities(boolean scanEntities) {
        this.scanEntities = scanEntities;
    }

    boolean scanEntities() {
        return scanEntities;
    }

    void setEntityManagerFactory(EntityManagerFactory entityManagerFactory, Map<String, String> emProps) {
        this.entityManagerFactory = entityManagerFactory;
        this.emProps = emProps;
    }

    @SuppressWarnings("unchecked")
    <T> Bean<T> getReference(BeanType beanType, Class<? super T> returnType) {
        if (!beanType.getType().isAssignableFrom(returnType)) {
            throw new IllegalArgumentException();
        }
        Bean<T> bean = (Bean<T>) getReference(beanType);
        if (bean != null && !returnType.isAssignableFrom(bean.getProxy().getClass())) {
            throw new IllegalArgumentException();
        }
        return bean;
    }

    Bean<?> getReference(BeanType beanType) {
        Bean<?> b = injectionPoints.get(beanType);
        if (b == null) {
            b = injectionPoints.entrySet()
                    .stream()
                    .filter(BeanManager::typedBean)
                    .filter(e -> e.getKey().equalsAsTyped(beanType))
                    .findFirst()
                    .map(Entry::getValue)
                    .orElse(null);
        }
        return b;
    }

    private static boolean typedBean(Entry<BeanType, Bean<?>> e) {
        return e.getKey().getType().isAnnotationPresent(Typed.class);
    }

    @SuppressWarnings("unchecked")
    Bean<?> createBean(BeanType beanType, Object beanDelegate) {
        if (!injectionPoints.containsKey(beanType)) {
            Object[] bd = {beanDelegate};
            CONTEXT.forEach(spi -> bd[0] = spi.bindContext(bd[0], beanType.getType()));
            injectionPoints.put(beanType, new Bean<>((Class<Object>) beanType.getType(), bd[0], tryToProxy(beanDelegate)));
        }
        return injectionPoints.get(beanType);
    }

    boolean contains(BeanType beanType) {
        return injectionPoints.containsKey(beanType);
    }

    boolean containsEntityManager() {
        return contains(EM_BEAN_TYPE);
    }

    EntityManager getCurrentEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("EntityManagerFactory not set");
        }
        return CURRENT_ENTITY_MANAGER.get(entityManagerFactory, emProps);
    }

    boolean isEntityManagerExistsOpen() {
        EntityManager em = CURRENT_ENTITY_MANAGER.get();
        return em != null && em.isOpen();
    }

    void clear() {
        injectionPoints.clear();
        CURRENT_ENTITY_MANAGER.remove();
        CONTEXT.forEach(ContextInjector::destroy);
    }

    static BeanType getEmBeanType() {
        return EM_BEAN_TYPE;
    }

    private Object tryToProxy(final Object beanDelegate) {
        if (beanDelegate == null) {
            return null;
        }
        final Class<?> type = beanDelegate.getClass();
        if (!isFinal(type.getModifiers()) && canMakeProxy(beanDelegate.getClass())) {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(type);
            Class<?> proxyClass = factory.createClass();
            try {
                Object proxy = proxyClass.newInstance();
                ((ProxyObject) proxy).setHandler((self, overridden, forwarder, args) -> {
                    boolean isAbstract = forwarder == null;
                    if (isAbstract) {
                        return null;
                    } else {
                        Transactional transactional = transactional(type, overridden);
                        if (transactional != null) {
                            EntityTransaction transaction = null;
                            SynchronizedEntityManager em = null;
                            if (canStartOrCloseTransaction()) {
                                Bean<SynchronizedEntityManager> emBean = getReference(EM_BEAN_TYPE, SynchronizedEntityManager.class);
                                if (emBean == null) {
                                    throw new InvalidTransactionException("no EntityManager found!");
                                }
                                em = emBean.getProxy();
                                transaction = em.unwrapTransaction();
                                if (canContinueWithTransaction(transactional, transaction, overridden)) {
                                    transaction.begin();
                                } else {
                                    transaction = null;
                                }
                            }
                            increaseTransactionBarriers();
                            try {
                                return overridden.invoke(beanDelegate, args);
                            } catch (Throwable e) {
                                if (transaction != null && transaction.isActive()) {
                                    transaction.rollback();
                                }
                                if (e instanceof InvocationTargetException) {
                                    Throwable cause = e.getCause();
                                    if (cause != null) {
                                        e = cause;
                                    }
                                }
                                throw e;
                            } finally {
                                decreaseTransactionBarriers();
                                if (transaction != null) {
                                    if (transaction.isActive()) {
                                        transaction.commit();
                                    }
                                    em.closeSafely();
                                }
                            }
                        } else {
                            try {
                                return overridden.invoke(beanDelegate, args);
                            } catch (InvocationTargetException e) {
                                Throwable cause = e.getCause();
                                throw cause == null ? e : cause;
                            }
                        }
                    }
                });
                return proxy;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Could not create proxy " + proxyClass, e);
            }
        } else {
            return beanDelegate;
        }
    }

    private boolean canMakeProxy(Class<?> type) {
        return hasManagedTransactions()
                && (hasAnnotationDeep(type, Transactional.class) /*|| Java Validation API*/);
    }

    private static Transactional transactional(Class<?> clazz, Method method) {
        Transactional t = getAnnotation(method, Transactional.class);
        return t == null ? getAnnotation(clazz, Transactional.class) : t;
    }

    private static boolean canContinueWithTransaction(Transactional config, EntityTransaction transaction, Method m) {
        switch (config.value()) {
            case REQUIRED:
                return true;
            case MANDATORY:
                if (!transaction.isActive()) {
                    throwTransactionalException(config.value(), m,
                            new TransactionRequiredException("called outside a transaction context"));
                }
                return false;
            case NEVER:
                if (transaction.isActive()) {
                    throwTransactionalException(config.value(), m,
                            new InvalidTransactionException("called inside a transaction context"));
                }
                return false;
            case SUPPORTS:
                return false;
            case REQUIRES_NEW: // not fully supported
                return true;
            case NOT_SUPPORTED: // not fully supported
                return false;
            default:
                throw new UnsupportedOperationException("Unknown enum " + config.value());
        }
    }

    private static void throwTransactionalException(TxType txType, Method m, Throwable cause) {
        throw new TransactionalException("Transaction not applicable to "
                + "@Transactional(value = Transactional.TxType." + txType + ") "
                + "in bean method call "
                + m.toGenericString()
                , cause);
    }
}
