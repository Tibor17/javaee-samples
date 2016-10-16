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
package producer;

import dao.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.*;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@ApplicationScoped
@SuppressWarnings("unused")
public class DaoProducer {

    @Produces
    @Dependent
    @DAO
    @SuppressWarnings("unchecked")
    public <T> IDAO<T> produceDaoWithIntegerId(@TransientReference InjectionPoint ip,
                                               @TransientReference BeanManager bm,
                                               @New @TransientReference IDAOFactory f) {
        return buildDaoWithOneGenericType(ip, bm, (IDAOFactory<T>) f);
    }

    @Produces
    @Dependent
    @DAO
    @SuppressWarnings("unchecked")
    public <T> LDAO<T> produceDaoWithLongId(@TransientReference InjectionPoint ip,
                                            @TransientReference BeanManager bm,
                                            @New @TransientReference LDAOFactory f) {
        return buildDaoWithOneGenericType(ip, bm, (LDAOFactory<T>) f);
    }

    @Produces
    @Dependent
    @Default
    @SuppressWarnings("unchecked")
    public <T> IDAO<T> produceUnqualifiedDaoWithIntegerId(@TransientReference InjectionPoint ip,
                                                          @TransientReference BeanManager bm,
                                                          @New @TransientReference IDAOFactory f) {
        return buildDaoWithOneGenericType(ip, bm, (IDAOFactory<T>) f);
    }

    @Produces
    @Dependent
    @Default
    @SuppressWarnings("unchecked")
    public <T> LDAO<T> produceUnqualifiedDaoWithLongId(@TransientReference InjectionPoint ip,
                                                       @TransientReference BeanManager bm,
                                                       @New @TransientReference LDAOFactory f) {
        return buildDaoWithOneGenericType(ip, bm, (LDAOFactory<T>) f);
    }

    @Produces
    @Dependent
    @DAO
    @SuppressWarnings("unchecked")
    public <T> DaoWithoutId<T> produceDaoWithoutId(@TransientReference InjectionPoint ip,
                                                   @TransientReference BeanManager bm,
                                                   @New @TransientReference DAOFactory f) {
        return buildDaoWithOneGenericType(ip, bm, (DAOFactory<T>) f);
    }

    @Produces
    @Dependent
    @Default
    @SuppressWarnings("unchecked")
    public <T> DaoWithoutId<T> produceUnqualifiedDaoWithoutId(@TransientReference InjectionPoint ip,
                                                              @TransientReference BeanManager bm,
                                                              @New @TransientReference DAOFactory f) {
        return buildDaoWithOneGenericType(ip, bm, (DAOFactory<T>) f);
    }

    @SuppressWarnings("unchecked")
    private static <E>
    DaoWithoutId<E> buildDaoWithOneGenericType(InjectionPoint ip, BeanManager bm, DAOFactory<E> factory) {
        Type daoType = ip.getType();
        if (daoType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) daoType;
            Type[] types = type.getActualTypeArguments();
            Class<E> entity = (Class<E>) types[0];
            EntityManager em = lookupEntityManager(ip, bm);
            return factory.build(entity, em);
        } else {
            throw new IllegalArgumentException("Use Generic Type in the interface "
                    + DaoWithoutId.class.getSimpleName()
                    + " in the injection point " + ip.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static <R extends INumericDAO<E, PK>, E, PK extends Number & Comparable<PK>>
    R buildDaoWithOneGenericType(InjectionPoint ip, BeanManager bm, GenericNumericDaoFactory<R, E, PK> factory) {
        Type daoType = ip.getType();
        if (daoType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) daoType;
            Type[] types = type.getActualTypeArguments();
            Class<E> entity = (Class<E>) types[0];
            EntityManager em = lookupEntityManager(ip, bm);
            return factory.build(entity, em);
        } else {
            throw new IllegalArgumentException("Use Generic Type in the interface "
                    + IDAO.class.getSimpleName()
                    + " or "
                    + LDAO.class.getSimpleName()
                    + " in the injection point " + ip.toString());
        }
    }

    private static <T extends Annotation> EntityManager lookupEntityManager(BeanManager bm, T qualifier) {
        Set<Bean<?>> beans = bm.getBeans(EntityManager.class, qualifier);
        Bean<?> bean = bm.resolve(beans);
        CreationalContext<?> ctx = bm.createCreationalContext(bean);
        return EntityManager.class.cast(bm.getReference(bean, EntityManager.class, ctx));
    }

    private static EntityManager lookupEntityManager(BeanManager bm, Class<? extends Annotation> qualifier) {
        Set<Bean<?>> beans = bm.getBeans(EntityManager.class, new AnnotationLiteral<Any>() {})
                .stream()
                .filter(bean -> hasQualifier(bean, qualifier))
                .collect(toSet());
        Bean<?> bean = bm.resolve(beans);
        CreationalContext<?> ctx = bm.createCreationalContext(bean);
        return EntityManager.class.cast(bm.getReference(bean, EntityManager.class, ctx));
    }

    private static EntityManager lookupEntityManager(InjectionPoint ip, BeanManager bm) {
        final Class def = Default.class;

        @SuppressWarnings("unchecked")
        final Class<? extends Annotation> annotation = ip.getQualifiers()
                .stream()
                .filter(q -> q.annotationType() == DAO.class)
                .map(q -> ((DAO) q).value())
                .findFirst()
                .orElse(def);

        if (bm.isQualifier(annotation)) {
            return lookupEntityManager(bm, annotation);
        } else {
            throw new ContextNotActiveException("no datasource qualifier nor stereotype presents in the " +
                    "injection point " + ip);
        }
    }

    private static boolean hasQualifier(Bean<?> bean, Class<? extends Annotation> qualifier) {
        return bean.getQualifiers()
                .stream()
                .anyMatch(a ->
                        (qualifier == Default.class || qualifier == Any.class) || a.annotationType() == qualifier);
    }

    interface GenericNumericDaoFactory<R extends INumericDAO<E, PK>, E, PK extends Number & Comparable<PK>> {
        R build(Class<E> entityType, EntityManager em);
    }

    interface GenericDaoFactory<E> {
        DaoWithoutId<E> build(Class<E> entityType, EntityManager em);
    }

    static class DAOFactory<E> implements GenericDaoFactory<E> {

        @Override
        public DaoWithoutId<E> build(Class<E> entityType, EntityManager em) {
            return new GenericDaoWithoutId<E>(entityType) {
                private static final long serialVersionUID = 1L;

                @Override
                protected EntityManager em() {
                    return em;
                }
            };
        }
    }

    static class IDAOFactory<E> implements GenericNumericDaoFactory<IDAO<E>, E, Integer> {

        @Override
        public IDAO<E> build(Class<E> entityType, EntityManager em) {
            class ID extends GenericNumericDAO<E, Integer> implements IDAO<E> {
                private static final long serialVersionUID = 1L;

                ID(Class<E> entityType) {
                    super(entityType, Integer.class);
                }

                @Override
                protected EntityManager em() {
                    return em;
                }
            }

            return new ID(entityType);
        }
    }

    static class LDAOFactory<E> implements GenericNumericDaoFactory<LDAO<E>, E, Long> {

        @Override
        public LDAO<E> build(Class<E> entityType, EntityManager em) {
            class LD extends GenericNumericDAO<E, Long> implements LDAO<E> {
                private static final long serialVersionUID = 1L;

                LD(Class<E> entityType) {
                    super(entityType, Long.class);
                }

                @Override
                protected EntityManager em() {
                    return em;
                }
            }

            return new LD(entityType);
        }
    }

}
