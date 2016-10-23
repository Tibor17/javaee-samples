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
package audit.persistence.service;

import audit.domain.Audit;
import audit.domain.AuditChange;
import audit.domain.AuditFlow;
import audit.domain.AuditHeader;
import audit.query.search.api.Matcher;
import audit.query.search.api.Sorter;
import audit.query.search.persistence.api.Predicates;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.MustJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.beans.BeanInfo;
import java.beans.Expression;
import java.beans.PropertyDescriptor;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static audit.query.search.persistence.api.Predicates.predicates;
import static com.querydsl.core.alias.Alias.$;
import static com.querydsl.core.alias.Alias.alias;
import static java.beans.Introspector.getBeanInfo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.lucene.search.SortField.Type.LONG;
import static org.apache.lucene.search.SortField.Type.STRING;
import static org.hibernate.search.jpa.Search.getFullTextEntityManager;

@ApplicationScoped
public class AuditService {
    private static final long TIMEOUT = 5L;

    private static final Invoker INVOKER = (target, beanProperty, value) -> {
        try {
            BeanInfo beanInfo = getBeanInfo(Audit.class);
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                if (beanProperty.equals(descriptor.getName())) {
                    new Expression(target, descriptor.getWriteMethod().getName(), new Object[] {value})
                            .execute();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    };

    @Inject
    private EntityManager em;

    public List<Audit> findAll() {
        return em.createNamedQuery("Audit.all", Audit.class)
                .getResultList();
    }

    public long count() {
        return em.createNamedQuery("Audit.count", Long.class)
                .getSingleResult();
    }

    @NotNull
    public Audit findAuditById(@NotNull String id) {
        return em.find(Audit.class, id);
    }

    @Transactional
    public void remove(Audit e) {
        em.remove(findAuditById(e.getId()));
    }

    @Transactional
    public void saveFlow(@NotNull Audit e) {
        e.getFlows().forEach(f -> {
            f.getHeaders().forEach(em::persist);
            f.getChanges().forEach(em::persist);
            em.persist(f);
        });
        em.persist(e);
    }

    @Transactional
    public void saveFlow(@NotNull Audit e, String error, @NotNull Collection<AuditHeader> headers,
                         @NotNull Collection<AuditChange> changes) {
        headers.forEach(em::persist);
        changes.forEach(em::persist);

        AuditFlow flow = new AuditFlow();
        flow.setError(error);
        flow.getHeaders().addAll(headers);
        flow.getChanges().addAll(changes);
        em.persist(flow);

        e.getFlows().add(flow);
        em.persist(e);
    }

    /**
     * .
     * javax.persistence.PersistenceException: org.hibernate.HibernateException: could not parse date string?2
     * Caused by: java.text.ParseException: Unparseable date: "?2"
     * The fix would be com.querydsl:querydsl-mongodb but it does NOT work with EntityManager
     *  but with MongoClient, see https://github.com/querydsl/querydsl/tree/master/querydsl-mongodb
     * https://github.com/mongodb/morphia
     * http://www.hascode.com/2014/02/creating-elegant-typesafe-queries-for-jpa-mongodbmorphia-and-lucene-using-querydsl/
     */
    public List<Audit> searchQueryDSL(long fromRownum, long maxRownums, @NotNull String module, @NotNull Calendar from,
                                      @NotNull Calendar to) {
        PathBuilder<Audit> entity = new PathBuilder<>(Audit.class, "audit");
        JPAQuery<Audit> q = new JPAQuery<Audit>(em).from(entity);
        Audit a = alias(Audit.class, entity);
        return q.where($(a.getModule()).eq(module))
                .where($(a.getStoredAt()).between(from, to))
                .offset(fromRownum)
                .limit(maxRownums)
                .fetch();
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public List<Audit> search(int fromRownum, int maxRownums,
                              Optional<Long> initiator,
                              Optional<String> module,
                              Optional<String> operationKey,
                              Optional<String> description,
                              Optional<Calendar> from, Optional<Calendar> to) {
        StringBuilder query = new StringBuilder("select a from Audit a where ");

        initiator.ifPresent(i ->
                query.append("a.initiator = ")
                        .append(i)
                        .append(" and "));

        module.ifPresent(m ->
                query.append("a.module = '")
                        .append(m)
                        .append("' and "));

        operationKey.ifPresent(ok ->
                query.append("a.operationKey = '")
                        .append(ok)
                        .append("' and "));

        description.map(String::trim)
                .map(d -> d.isEmpty() ? null : d)
                .ifPresent(d ->
                        query.append("a.description like '%")
                                .append(d)
                                .append("%' and "));

        if (from.isPresent() && to.isPresent()) {
            query.append("a.storedAt between :from and :to");
        } else if (from.isPresent()) {
            query.append("a.storedAt >= :from");
        } else if (to.isPresent()) {
            query.append("a.storedAt <= :to");
        }  else {
            int rem = query.lastIndexOf(" and ");
            if (rem != -1) {
                query.delete(rem, query.length());
            }
        }

        TypedQuery<Audit> audits = em.createQuery(query.toString(), Audit.class)
                .setFirstResult(fromRownum)
                .setMaxResults(maxRownums);

        from.ifPresent(f -> audits.setParameter("from", f));
        to.ifPresent(t -> audits.setParameter("to", t));

        return audits.getResultList();
    }

    public List<Audit> searchAuditPhrase(int fromRownum, int maxRownums, @NotNull UnaryOperator<Predicates> where) {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Audit.class)
                .get();

        Function<Matcher<?>, Query> query = m ->
                qb.phrase()
                        .onField(m.getFieldName())
                        .sentence(m.getSearchedText() == null ? null : m.getSearchedText().toString())
                        .createQuery();

        BiFunction<MustJunction, Matcher<?>, MustJunction> and = (j, m) -> j.must(query.apply(m));

        Predicates p = where.apply(predicates());

        MustJunction junction =
                p.getMatchers()
                .stream()
                .reduce(null, (j, m) -> j == null ? qb.bool().must(query.apply(m)) : and.apply(j, m), (a, b) -> a);

        Query luceneQuery = junction.createQuery();

        FullTextQuery ftq = fullTextEntityManager.createFullTextQuery(luceneQuery, Audit.class);

        Sort sort = toSort(p.getSorters());
        if (sort.getSort().length != 0) {
            ftq.setSort(sort);
        }

        return ftq.setFirstResult(fromRownum)
                .setMaxResults(maxRownums)
                .getResultList();
    }

    public List<Audit> searchAuditLike(int fromRownum, int maxRownums, @NotNull UnaryOperator<Predicates> where) {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Audit.class)
                .get();

        Function<Matcher<?>, Query> query = m -> {
            Audit e = new Audit();
            INVOKER.writeProperty(e, m.getFieldName(), m.getSearchedText());
            return qb.moreLikeThis()
                    .comparingField(m.getFieldName())
                    .toEntity(e)
                    .createQuery();
        };

        BiFunction<MustJunction, Matcher<?>, MustJunction> and = (j, m) -> j.must(query.apply(m));

        Predicates p = where.apply(predicates());

        MustJunction junction =
                p.getMatchers()
                        .stream()
                        .reduce(null, (j, m) -> j == null ? qb.bool().must(query.apply(m)) : and.apply(j, m), (a, b) -> a);

        Query luceneQuery = junction.createQuery();

        FullTextQuery ftq = fullTextEntityManager.createFullTextQuery(luceneQuery, Audit.class);
        ftq.limitExecutionTimeTo(TIMEOUT, SECONDS);

        Sort sort = toSort(p.getSorters());
        if (sort.getSort().length != 0) {
            ftq.setSort(sort);
        }

        return ftq.setFirstResult(fromRownum)
                .setMaxResults(maxRownums)
                .getResultList();
    }

    public List<AuditFlow> searchAuditFlowPhrase(String textSearch, int fromRownum, int maxRownums) {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(AuditFlow.class)
                .get();

        Query luceneQuery = qb.phrase()
                .onField("error")
                .sentence(textSearch)
                .createQuery();

        return fullTextEntityManager.createFullTextQuery(luceneQuery, AuditFlow.class)
                .setFirstResult(fromRownum)
                .setMaxResults(maxRownums)
                .getResultList();
    }

    private static SortField toSortField(Sorter sorter) {
        Class<?> ft = sorter.getFieldType();
        if (ft == String.class) {
            return new SortField(sorter.getFieldName(), STRING, !sorter.isAscending());
        } else if (ft == long.class || ft == Long.class || ft == Date.class || ft == Calendar.class) {
            return new SortField(sorter.getFieldName(), LONG, !sorter.isAscending());
        } else {
            throw new IllegalStateException("no mapped field type " + ft);
        }
    }

    private static Sort toSort(Collection<Sorter<?>> sorters) {
        Collection<SortField> fields = sorters.stream()
                .map(AuditService::toSortField)
                .collect(toList());
        int count = fields.size();
        return new Sort(fields.toArray(new SortField[count]));
    }
}
