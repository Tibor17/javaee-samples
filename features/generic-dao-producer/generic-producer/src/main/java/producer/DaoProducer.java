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
import dao.IDAO;
import dao.QualifiedJPA;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@ApplicationScoped
public class DaoProducer {
    @Produces
    @Dependent
    @DAO
    public <T> IDAO<T, Long> produceDaoWithLong(InjectionPoint ip, BeanManager bm) {
        return buildDao(ip, bm);
    }

    @Produces
    @Dependent
    @DAO
    public <T> IDAO<T, Integer> produceDaoWithInteger(@TransientReference InjectionPoint ip, @TransientReference BeanManager bm) {
        return buildDao(ip, bm);
    }

    @SuppressWarnings("unchecked")
    private static <T, PK extends Number & Comparable<PK>> IDAO<T, PK> buildDao(InjectionPoint ip, BeanManager bm) {
        Type t1 = ip.getType();
        if (t1 instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) t1;
            Type[] types = type.getActualTypeArguments();
            Class<T> entity = (Class<T>) types[0];
            Class<PK> id = (Class<PK>) types[1];
            EntityManager em = lookupEntityManager(bm, lookupQualifier(ip));
            return new GenericDAO<T, PK>(entity, id) {
                @Override
                protected EntityManager em() {
                    return em;
                }
            };
        } else {
            throw new IllegalArgumentException("Annotation @Dao is required when injecting BaseDao");
        }
    }

    private static <T extends Annotation> EntityManager lookupEntityManager(BeanManager bm, T qualifier) {
        Set<Bean<?>> beans = bm.getBeans(EntityManager.class, qualifier);
        Bean bean = bm.resolve(beans);
        CreationalContext ctx = bm.createCreationalContext(bean);
        return EntityManager.class.cast(bm.getReference(bean, EntityManager.class, ctx));
    }

    private static Annotation lookupQualifier(InjectionPoint ip) {
        Set<Annotation> qualifiers = new HashSet<>();

        asList(ip.getMember().getDeclaringClass().getAnnotations())
                .stream()
                .filter(q -> q.annotationType().isAnnotationPresent(QualifiedJPA.class))
                .forEach(qualifiers::add);

        if (qualifiers.size() != 1) {
            throw new IllegalStateException();
        }

        return qualifiers.iterator().next();
    }
}
