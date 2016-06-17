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
package service;

import audit.domain.Audit;
import audit.domain.AuditChange;
import audit.domain.AuditFlow;
import audit.domain.AuditHeader;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.querydsl.core.alias.Alias.$;
import static com.querydsl.core.alias.Alias.alias;

@ApplicationScoped
public class AuditService {
    @Inject
    EntityManager em;

    public List<Audit> findAll() {
        return em.createNamedQuery("Audit.all", Audit.class)
                .getResultList();
    }

    public long count() {
        return em.createNamedQuery("Audit.count", Long.class)
                .getSingleResult();
    }

    public @NotNull Audit findAuditById(@NotNull String id) {
        return em.find(Audit.class, id);
    }

    @Transactional
    public void remove(Audit e) {
        em.remove(findAuditById(e.getId()));
    }

    @Transactional
    public void saveFlow(@NotNull Audit e) {
        em.persist(e);
    }

    @Transactional
    public void saveFlow(@NotNull Audit e, String error, @NotNull Collection<AuditHeader> headers,
                         @NotNull Collection<AuditChange> changes) {
        AuditFlow flow = new AuditFlow();
        flow.setError(error);
        flow.getHeaders().addAll(headers);
        flow.getChanges().addAll(changes);
        e.getFlows().add(flow);
        saveFlow(e);
    }

    public List<Audit> search(long fromRownum, long maxRownums, @NotNull String module,
                              @NotNull Calendar from, @NotNull Calendar to) {
        PathBuilder<Audit> entity = new PathBuilder<>(Audit.class, "audit");
        JPAQuery<Audit> q = new JPAQuery<Audit>(em).from(entity);
        Audit a = alias(Audit.class, entity);
        return q.where($(a.getModule()).eq(module))
                .where($(a.getStoredAt()).between(from, to))
                .offset(fromRownum)
                .limit(maxRownums)
                .fetch();
    }

    public List<Audit> search(int fromRownum, int maxRownums,
                              Optional<Long> initiator,
                              Optional<String> module,
                              Optional<String> operationKey,
                              Optional<String> description,
                              Optional<Calendar> from, Optional<Calendar> to) {
        PathBuilder<Audit> entity = new PathBuilder<>(Audit.class, "audit");
        JPAQuery<Audit> q = new JPAQuery<Audit>(em).from(entity);
        Audit a = alias(Audit.class, entity);

        initiator.ifPresent(i -> q.where($(a.getInitiator()).eq(i)));

        module.ifPresent(m -> q.where($(a.getModule()).eq(m)));

        operationKey.ifPresent(ok -> q.where($(a.getOperationKey()).eq(ok)));

        description.map(String::trim)
                .map(d -> d.isEmpty() ? null : d)
                .ifPresent(d -> q.where($(a.getDescription()).containsIgnoreCase(d)));

        if (from.isPresent() && to.isPresent()) {
            q.where($(a.getStoredAt()).between(from.get(), to.get()));
        } else if (from.isPresent()) {
            q.where($(a.getStoredAt()).goe(from.get()));
        } else if (to.isPresent()) {
            q.where($(a.getStoredAt()).loe(to.get()));
        }

        return q.offset(fromRownum)
                .limit(maxRownums)
                .fetch();
    }
}
