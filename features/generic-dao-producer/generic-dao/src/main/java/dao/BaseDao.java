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

import com.querydsl.core.FilteredClause;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.PessimisticLockException;
import javax.persistence.TransactionRequiredException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

interface BaseDao<E> extends Serializable {

    /**
     * Generic query.
     *
     * @param q      QueryDSL query
     * @param <Q>    customized return type, a value returned by {@link Queries.I1#$(Object)}
     * @return customized value returned by {@link Queries.I1#$(Object)}
     */
    <Q> Q query(@NotNull Queries.I1<Query<E>> q);

    /**
     * @see PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attributes are loaded
     */
    boolean isLoaded(@NotNull E entityObject);

    /**
     * @param entityObject entity instance
     * @throws IllegalArgumentException if the instance is not an entity
     */
    void detach(@NotNull E entityObject);

    /**
     * Refresh from database, (optional) overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return returns <code>e</code> to be compliant with {@link IGDAO}
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity is not attached
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws TransactionRequiredException see {@link EntityManager#refresh(Object)}
     * @see {@link EntityManager#refresh(Object)}
     */
    @NotNull
    E refresh(@NotNull E e);

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return returns <code>e</code> to be compliant with {@link IGDAO}
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity is not attached
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
    @NotNull
    E refresh(@NotNull E e, @NotNull LockModeType lockMode);

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Use the timeout if the default one elapsed too fast.
     *
     * @return returns <code>e</code> to be compliant with {@link IGDAO}
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity is not attached
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
    @NotNull
    E refresh(@NotNull E e, @NotNull LockModeType lockMode, @NotNull TimeUnit timeoutUnits, @Min(0) long timeout);

    /**
     * @see PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attribute was loaded
     */
    boolean isLoaded(@NotNull E entityObject, @NotNull String attributeName);

    boolean isAttached(@NotNull E entityObject);

    @Min(0) long count();

    @Min(0) long count(@NotNull Where<E> predicate);

    @Min(0) long count(@NotNull Function<E, Predicate> predicate);

    /**
     * Persists the <tt>newInstance</tt> object into database.
     * Primary key is stored in <tt>newInstance</tt>.
     * Fail-Fast.
     */
    void save(@NotNull E newInstance);

    /**
     * Persists the <tt>newInstance</tt> object into database.
     * Primary key is stored in <tt>newInstance</tt>.
     * Fail-Fast.
     */
    void save(@NotNull Supplier<E> newInstance);

    /**
     * Merges given changes to the returned object.
     *
     * @param mergeFrom entity attached or detached to the persistence context. The entity object is returned detached.
     * @return newly merged entity, attached to the persistence context
     */
    @NotNull
    E merge(@NotNull E mergeFrom);

    /**
     * This method is eligible for been intercepted in proxyable bean.
     * By proxying this method you can compare old entity with newly returned object in the interceptor.
     */
    @NotNull
    E merge(@NotNull Supplier<E> mergeFrom);

    /**
     * Retrieves all objects that were previously persisted to the database.
     */
    @NotNull
    List<E> loadAll();

    @NotNull
    List<E> loadAll(@Min(0) int pagingOffset, @Min(1) int pageSize);

    @NotNull
    List<E> loadAll(@Min(0) int pagingOffset, @Min(1) int pageSize, @NotNull Collection<Predicate> predicates);

    @NotNull
    List<E> loadAll(@NotNull Collection<Predicate> predicates);

    @NotNull
    List<E> loadAll(@Min(0) int pagingOffset, @Min(1) int pageSize, @NotNull Where<E> predicate);

    @NotNull
    List<E> loadAll(@NotNull Where<E> predicate);

    List<E> loadAll(@NotNull BiConsumer<JPAQuery, E> predicate);

    List<E> loadAll(@NotNull Function<E, Predicate> predicate);

    E load(@NotNull Where<E> predicate);

    E load(@NotNull BiConsumer<JPAQuery, E> predicate);

    E load(@NotNull Function<E, Predicate> predicate);

    long delete(@NotNull BiConsumer<FilteredClause<?>, E> predicate);

    /**
     * Remove an object from persistent storage in the database.
     *
     * @throws IllegalArgumentException     if the instance is not an
     *                                      entity or is a detached entity
     * @throws TransactionRequiredException if invoked on a
     *                                      container-managed entity manager of type
     *                                      <code>PersistenceContextType.TRANSACTION</code> and there is
     *                                      no transaction
     */
    void delete(@NotNull E e);

    long deleteAll();

    @Min(0)
    int updateByNamedQuery(String sqlStatement);

    @Min(0)
    int updateByNamedQuery(@NotNull String sqlStatement, Object... attributes);

    @Min(0)
    int updateByNamedQuery(@Size String sqlStatement, @Size String attributeName, Object attributeValue);

    @Min(0)
    int updateByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes);

    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement);

    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass);

    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass,
                                   @Min(0) int paginationOffset, @Min(1) int pageSize);

    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement, Object... attributes);

    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass, Object... attributes);

    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement, @NotNull String attributeName, Object attributeValue);

    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull String attributeName, Object attributeValue, @NotNull Class<T> resultClass);

    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes);

    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes, @NotNull Class<T> resultClass);

    @NotNull
    List<E> findByAttributeAsPattern(@NotNull String attributeName, @NotNull String pattern);

    @NotNull
    List<E> findByAttributeAsString(@NotNull String attributeName, @NotNull String attributeValue, boolean ignoreCase);
}
