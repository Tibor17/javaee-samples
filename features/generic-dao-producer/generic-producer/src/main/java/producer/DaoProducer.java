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
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.stream.Collectors.toSet;

@ApplicationScoped
@SuppressWarnings("unused")
public class DaoProducer {

    @Produces
    @Dependent
    @DAO
    public <T> IDAO<T> produceDaoWithLongId(@TransientReference InjectionPoint ip,
                                            @TransientReference BeanManager bm) {
        return buildDaoWithOneGenericType(ip, bm, new IDAOFactory<>());
    }

    @Produces
    @Dependent
    @DAO
    public <T> LDAO<T> produceDaoWithIntegerId(@TransientReference InjectionPoint ip,
                                               @TransientReference BeanManager bm) {
        return buildDaoWithOneGenericType(ip, bm, new LDAOFactory<>());
    }

    @SuppressWarnings("unchecked")
    private static <R extends INumericDAO<E, PK>, E, PK extends Number & Comparable<PK>>
    R buildDaoWithOneGenericType(InjectionPoint ip, BeanManager bm, GenericNumericDaoFactory<R, E, PK> factory) {

        Type daoType = ip.getType();
        if (daoType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) daoType;
            Type[] types = type.getActualTypeArguments();
            Class<E> entity = (Class<E>) types[0];
            //Class<PK> id = (Class<PK>) types[1];
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
        Class<? extends Annotation> annotation = ip.getQualifiers()
                .stream()
                .filter(q -> q.annotationType() == DAO.class)
                .map(q -> ((DAO) q).value())
                .findFirst()
                .get();

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

    static class IDAOFactory<E> implements GenericNumericDaoFactory<IDAO<E>, E, Integer> {

        @Override
        public IDAO<E> build(Class<E> entityType, EntityManager em) {
            class ID extends GenericNumericDAO<E, Integer> implements IDAO<E> {

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
