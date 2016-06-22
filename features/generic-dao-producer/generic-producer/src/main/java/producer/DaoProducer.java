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

import dao.DAO;
import dao.GenericDAO;
import dao.IGDAO;

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
    public <T> IGDAO<T, Long> produceDaoWithLongId(@TransientReference InjectionPoint ip,
                                                  @TransientReference BeanManager bm) {
        return buildDao(ip, bm);
    }

    @Produces
    @Dependent
    @DAO
    public <T> IGDAO<T, Integer> produceDaoWithIntegerId(@TransientReference InjectionPoint ip,
                                                        @TransientReference BeanManager bm) {
        return buildDao(ip, bm);
    }

    @SuppressWarnings("unchecked")
    private static <T, PK extends Number & Comparable<PK>> IGDAO<T, PK> buildDao(InjectionPoint ip, BeanManager bm) {
        Type daoType = ip.getType();
        if (daoType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) daoType;
            Type[] types = type.getActualTypeArguments();
            Class<T> entity = (Class<T>) types[0];
            Class<PK> id = (Class<PK>) types[1];
            EntityManager em = lookupEntityManager(ip, bm);
            return new GenericDAO<T, PK>(entity, id) {
                @Override
                protected EntityManager em() {
                    return em;
                }
            };
        } else {
            throw new IllegalArgumentException("Use Generic Type in the interface "
                    + IGDAO.class.getSimpleName()
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
}
