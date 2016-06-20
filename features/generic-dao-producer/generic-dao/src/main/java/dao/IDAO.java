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

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IDAO<E, PK extends Number & Comparable<PK>> {
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
     * Refresh (detached) entity object.
     *
     * @param entityObject (detached) entity object to refresh from
     * @return never returns null
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} does not have id, or
     *                                  the object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     */
    @NotNull
    E reload(@NotNull E entityObject);

    /**
     * Refresh (detached) entity object.
     *
     * @param entityObject (detached) entity object to refresh from
     * @return never returns null
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if the entity object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     */
    @NotNull
    E reloadIfDetached(@NotNull E entityObject);

    /**
     * Behaves the same as {@link EntityManager#getReference EntityManager.getReference}.
     */
    @NotNull
    E fetchEager(@NotNull E entityObject);

    /**
     * Refresh from database, (optional) overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means 5 seconds if timeout elapsed too fast.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} does not have id, or
     *                                  the object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @see {@link EntityManager#refresh(Object)}
     */
    @NotNull
    E refresh(@NotNull E e);

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means 5 seconds if timeout elapsed too fast.
     *
     * @return refreshed entity object: new attached object if given argument {@code e} was detached, or given entity
     * reference if argument {@code e} is attached to the persistence context.
     * @throws EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} does not have id, or
     *                                  the object is found not to be an entity
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @see {@link EntityManager#refresh(Object, LockModeType)}
     */
    @NotNull
    E refresh(@NotNull E e, @NotNull LockModeType lockMode);

    /**
     * @see PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attribute was loaded
     */
    boolean isLoaded(@NotNull E entityObject, @NotNull String attributeName);

    /**
     * @see PersistenceUnitUtil#getIdentifier(Object) ID of given entity
     */
    @NotNull
    PK getIdentifier(@NotNull E entityObject);

    boolean hasId(@NotNull E entityObject);

    boolean isAttached(@NotNull E entityObject);

    long count();

    long count(Where<E> predicate);

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
     * This method is eligible for been intercepted in proxyable bean.
     * By proxying this method you can compare old entity with newly returned object in the interceptor.
     * The function {@link Function} consumes entity object which appears in the persistence context.
     */
    @NotNull
    E update(@NotNull PK id, @NotNull Function<E, E> merge);

    /**
     * @see EntityManager#getReference(Class, Object) load state lazily
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
     * Retrieves all objects that were previously persisted to the database.
     */
    @NotNull
    List<E> loadAll();

    @NotNull
    List<E> loadAll(@Min(0) int page, @Min(1) int pageSize);

    @NotNull
    List<E> loadAll(@Min(0) int page, @Min(1) int pageSize, @NotNull Collection<Predicate> predicates);

    @NotNull
    List<E> loadAll(@NotNull Collection<Predicate> predicates);

    @NotNull
    List<E> loadAll(@Min(0) int page, @Min(1) int pageSize, @NotNull Where<E> predicate);

    @NotNull
    List<E> loadAll(@NotNull Where<E> predicate);

    List<E> loadAll(@NotNull BiConsumer<JPAQuery, E> predicate);

    E load(@NotNull Where<E> predicate);

    E load(@NotNull BiConsumer<JPAQuery, E> predicate);

    /**
     * Remove an object from persistent storage in the database.
     */
    void delete(@NotNull PK id);

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
