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
package dao;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

abstract class GenericDAO<E, PK extends Serializable & Comparable<PK>>
        extends BaseDaoImpl<E> implements IGDAO<E, PK> {

    private final Class<PK> primaryKeyType;

    @SuppressWarnings("unchecked")
    protected GenericDAO() {
        super(GenericDAO.class, true);
        primaryKeyType = (Class<PK>) requireNonNull(optionalSecondGenericParameter);
    }

    protected GenericDAO(Class<E> entityType, Class<PK> primaryKeyType) {
        super(entityType);
        this.primaryKeyType = primaryKeyType;
    }

    protected
    @NotNull
    Class<PK> getPrimaryKeyType() {
        return primaryKeyType;
    }

    /**
     * Behaves the same as {@link EntityManager#getReference EntityManager.getReference}.
     */
    @Override
    public
    @NotNull
    E fetchLazily(@NotNull PK id) {
        return em().getReference(getEntityType(), id);
    }

    /**
     * @see PersistenceUnitUtil#getIdentifier(Object) ID of given entity
     */
    @Override
    public
    @NotNull
    PK getIdentifier(@NotNull E entityObject) {
        return primaryKeyType.cast(em().getEntityManagerFactory().getPersistenceUnitUtil()
                .getIdentifier(entityObject));
    }

    @Override
    public boolean hasId(@NotNull E entityObject) {
        PK id = getIdentifier(entityObject);
        return id != null && (!(id instanceof Number) || ((Number) id).longValue() > 0);
    }

    /**
     * This method is eligible for been intercepted in proxyable bean.
     * By proxying this method you can compare old entity with newly returned object in the interceptor.
     * The function {@link Function} consumes entity object which appears in the persistence context.
     */
    @Override
    public
    @NotNull
    E update(@NotNull PK id, @NotNull Function<E, E> merge) {
        E old = load(id);
        E neW = merge.apply(old);
        return em().merge(neW);
    }

    /**
     * @see EntityManager#getReference(Class, Object) load state lazily
     */
    @Override
    public
    @NotNull
    E loadReference(@NotNull PK id) {
        return em().getReference(getEntityType(), id);
    }

    /**
     * Retrieves an object that was previously persisted to the database
     * using the indicated id as primary key.
     */
    @Override
    public E load(@NotNull PK id) {
        return em().find(getEntityType(), id);
    }

    @Override
    public E load(@NotNull PK id, @NotNull LockModeType lock) {
        return em().find(getEntityType(), id, lock);
    }

    /**
     * Loads (detached) entity object into persistence context (unless already been loaded).
     *
     * @param entityObject (detached) entity object to load
     * @return never returns null
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} does not have id, or
     *                                  the object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     */
    @Override
    public
    @NotNull
    E reload(@NotNull E entityObject) {
        final PK id;
        if (!hasId(entityObject) || (id = getIdentifier(entityObject)) == null) {
            throw new IllegalArgumentException("does not have id");
        }
        final E e = load(id);
        if (e == null) {
            throw new EntityNotFoundException(getEntityType().getSimpleName()
                    + " record with "
                    + id
                    + " does not exist in database");
        }
        return e;
    }

    /**
     * Loads (detached) entity object into persistence context (unless already been loaded).
     *
     * @param entityObject (detached) entity object to load
     * @return never returns null
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if the entity object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     */
    @Override
    public
    @NotNull
    E reloadIfDetached(@NotNull E entityObject) {
        return reloadIfDetached(e -> !hasId(e) || isAttached(e), entityObject);
    }

    protected E reloadIfDetached(Predicate<E> simplyReturn, @NotNull E entityObject) {
        return simplyReturn.test(entityObject) ? entityObject : reload(entityObject);
    }

    /**
     * Refresh from database, (optional) overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity object does not have id
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws TransactionRequiredException see {@link EntityManager#refresh(Object)}
     * @see {@link EntityManager#refresh(Object)}
     */
    @Override
    public
    E refresh(E e) {
        e = reloadIfDetached(e);
        em().refresh(e);
        return e;
    }

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity object does not have id
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws TransactionRequiredException see {@link EntityManager#refresh(Object, LockModeType)}
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @see {@link EntityManager#refresh(Object, LockModeType)}
     */
    @Override
    public
    E refresh(E e, LockModeType lockMode) {
        e = reloadIfDetached(e);
        em().refresh(e, lockMode);
        return e;
    }

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Use the timeout if the default one elapsed too fast.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity object does not have id
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws TransactionRequiredException see {@link EntityManager#refresh(Object, LockModeType, Map)}
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @see {@link EntityManager#refresh(Object, LockModeType, Map)}
     */
    @Override
    public
    E refresh(E e, LockModeType lockMode, TimeUnit timeoutUnits, long timeout) {
        e = reloadIfDetached(e);
        em().refresh(e, lockMode);
        return e;
    }

    /**
     * Remove an object from persistent storage in the database.
     */
    @Override
    public void delete(@NotNull PK id) {
        em().remove(load(id));
    }
}
