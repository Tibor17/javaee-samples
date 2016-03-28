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
package audit.service;

import audit.domain.Audit;
import audit.domain.AuditChange;
import audit.domain.AuditFlow;
import audit.domain.AuditHeader;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

import static org.hibernate.search.jpa.Search.getFullTextEntityManager;

@ApplicationScoped
public class AuditService {
    @Inject
    EntityManager em;

    public @NotNull Audit findAuditById(@NotNull String id) {
        return em.find(Audit.class, id);
    }

    @Transactional
    public void saveFlow(@NotNull Audit e, String error, @NotNull Collection<AuditHeader> headers, @NotNull Collection<AuditChange> changes) {
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

    public @SuppressWarnings("unchecked") List<Audit> searchAuditPhrase(String textSearch) {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Audit.class)
                .get();

        Query luceneQuery = qb.phrase()
                .onField("module")
                //.andField("initiator")
                .sentence(textSearch)
                .createQuery();

        return fullTextEntityManager.createFullTextQuery(luceneQuery, Audit.class)
                .getResultList();
    }

    public @SuppressWarnings("unchecked") List<AuditFlow> searchAuditFlowPhrase(String textSearch) {
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
                .getResultList();
    }

    public @SuppressWarnings("unchecked") List<Audit> searchAuditEntity(String textSearch) {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Audit.class)
                .get();

        Audit e = new Audit();
        e.setModule(textSearch);

        Query luceneQuery = qb.moreLikeThis()
                .comparingFields("module")
                .toEntity(e)
                .createQuery();


        return fullTextEntityManager.createFullTextQuery(luceneQuery, Audit.class)
                .getResultList();
    }
}
