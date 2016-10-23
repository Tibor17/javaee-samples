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

import javax.persistence.LockModeType;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public interface IGDAO<E, PK extends Serializable & Comparable<PK>>
        extends BaseDao<E> {

    /**
     * Behaves the same as {@link javax.persistence.EntityManager#getReference EntityManager.getReference}.
     */
    @NotNull
    E fetchLazily(@NotNull PK id);

    /**
     * @see javax.persistence.PersistenceUnitUtil#getIdentifier(Object) ID of given entity
     */
    @NotNull
    PK getIdentifier(@NotNull E entityObject);

    /**
     * This method is eligible for been intercepted in proxyable bean.
     * By proxying this method you can compare old entity with newly returned object in the interceptor.
     * The function {@link Function} consumes entity object which appears in the persistence context.
     */
    @NotNull
    E update(@NotNull PK id, @NotNull Function<E, E> merge);

    /**
     * @see javax.persistence.EntityManager#getReference(Class, Object) load state lazily
     */
    @NotNull
    E loadReference(@NotNull PK id);

    /**
     * Retrieves an object that was previously persisted to the database
     * using the indicated id as primary key.
     */
    E load(@NotNull PK id);

    E load(@NotNull PK id, @NotNull LockModeType lock);

    /**
     * Load (detached) entity object.
     *
     * @param entityObject (detached) entity object to find (load)
     * @return never returns null
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} does not have id, or
     *                                  the object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     */
    @NotNull
    E reload(@NotNull E entityObject);

    /**
     * Load (detached) entity object only if detached.
     *
     * @param entityObject (detached) entity object to refresh from
     * @return never returns null
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if the entity object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     */
    @NotNull
    E reloadIfDetached(@NotNull E entityObject);

    /**
     * Refresh from database, (optional) overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity object does not have id
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @see {@link javax.persistence.EntityManager#refresh(Object)}
     */
    @Override
    E refresh(E e);

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity object does not have id
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @see {@link javax.persistence.EntityManager#refresh(Object, LockModeType)}
     */
    @Override
    E refresh(E e, LockModeType lockMode);

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Use the timeout if the default one elapsed too fast.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity object does not have id
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws javax.persistence.TransactionRequiredException see
     * {@link javax.persistence.EntityManager#refresh(Object, LockModeType, java.util.Map)}
     * @throws javax.persistence.PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws javax.persistence.LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws javax.persistence.PersistenceException if an unsupported lock call
     *         is made
     * @see {@link javax.persistence.EntityManager#refresh(Object, LockModeType, java.util.Map)}
     */
    @Override
    E refresh(E e, LockModeType lockMode, TimeUnit timeoutUnits, long timeout);

    boolean hasId(@NotNull E entityObject);

    /**
     * Remove an object from persistent storage in the database.
     */
    void delete(@NotNull PK id);
}
