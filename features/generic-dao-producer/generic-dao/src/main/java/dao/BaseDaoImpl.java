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
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAQueryBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.querydsl.sql.CUBRIDTemplates;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLServer2005Templates;
import com.querydsl.sql.SQLServer2012Templates;
import com.querydsl.sql.SQLServerTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLiteTemplates;
import com.querydsl.sql.TeradataTemplates;
import com.querydsl.sql.dml.SQLInsertClause;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.querydsl.core.alias.Alias.alias;
import static com.querydsl.sql.SQLTemplates.DEFAULT;
import static java.beans.Introspector.decapitalize;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

abstract class BaseDaoImpl<E> implements BaseDao<E> {

    /**
     * The type of entity the dao is able to work with.
     *
     * @serial
     */
    private final Class<E> entityType;

    /**
     * Used only in constructor of {@link GenericDAO}.
     *
     * @transient
     */
    private Type optionalSecondGenericParameter;

    protected BaseDaoImpl(Class<E> entityType) {
        this.entityType = entityType;
    }

    final Type getOptionalSecondGenericParameter() {
        return optionalSecondGenericParameter;
    }

    @SuppressWarnings("unchecked")
    BaseDaoImpl(Class<?> genericClass, boolean secondGenericParameter) {
        ParameterizedType parameterizedType = null;
        for (Class<?> parent = getClass(); parent != genericClass; parent = parent.getSuperclass()) {
            Type type = parent.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType) type;
                break;
            }
        }
        Type[] genericTypes = requireNonNull(parameterizedType, "java generic types missing").getActualTypeArguments();
        entityType = (Class<E>) genericTypes[0];
        optionalSecondGenericParameter = secondGenericParameter ? genericTypes[1] : null;
    }

    @NotNull
    protected abstract
    EntityManager em();

    Query<E> createQuery() {
        return new Query<>(newQuery(), newQueryEntity(), alias(getEntityType()));
    }

    @NotNull
    protected
    Class<E> getEntityType() {
        return entityType;
    }

    @NotNull
    protected
    PathBuilder<E> newQueryEntity() {
        return newQueryEntity(decapitalize(getEntityType().getSimpleName()));
    }

    @NotNull
    protected
    PathBuilder<E> newQueryEntity(@NotNull @Size(min = 1) String alias) {
        return new PathBuilder<E>(getEntityType(), alias);
    }

    @NotNull
    protected
    JPAQuery<E> newQuery() {
        return new JPAQuery<>(em());
    }

    @NotNull
    protected
    JPAQuery newSubQuery() {
        return new JPAQuery();
    }

    @NotNull
    protected
    JPADeleteClause newDeleteClause(@NotNull EntityPath<?> q) {
        return new JPADeleteClause(em(), q);
    }

    @NotNull
    protected
    JPAUpdateClause newUpdateClause(@NotNull EntityPath<?> q) {
        return new JPAUpdateClause(em(), q);
    }

    @NotNull
    protected
    SQLInsertClause newSQLInsertClause(@NotNull Connection connection, @NotNull SQLTemplates templates,
                                       @NotNull RelationalPath<?> q) throws SQLException {
        return new SQLInsertClause(connection, templates, q);
    }

    @NotNull
    protected
    SQLInsertClause newSQLInsertClause(@NotNull Connection connection, @NotNull RelationalPath<?> q)
            throws SQLException {
        return new SQLInsertClause(connection, findTemplates(connection), q);
    }

    @NotNull
    protected
    SQLInsertClause newSQLInsertClause(@NotNull RelationalPath<?> q) throws SQLException {
        EntityManager em = em();
        // JPQLTemplates templates = JPAProvider.getTemplates(em);
        Connection connection = em.unwrap(Connection.class);
        return new SQLInsertClause(connection, findTemplates(connection), q);
    }

    @NotNull
    protected
    SQLQuery newSQLQuery(@NotNull Connection connection, @NotNull SQLTemplates templates) {
        return new SQLQuery(connection, templates);
    }

    @NotNull
    protected
    SQLQuery newSQLQuery(@NotNull SQLTemplates templates) {
        return new SQLQuery(templates);
    }

    /**
     *
     * @param q      {@inheritDoc}
     * @param <Q>    {@inheritDoc}
     * @return {@inheritDoc}
     */
    public <Q> Q query(Queries.I1<Query<E>> q) {
        return q.$(createQuery());
    }

    /**
     * @see javax.persistence.PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attributes are loaded
     */
    @Override
    public boolean isLoaded(@NotNull E entityObject) {
        return em().getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entityObject);
    }

    /**
     * @param entityObject entity instance
     * @throws IllegalArgumentException if the instance is not an entity
     */
    @Override
    public void detach(@NotNull E entityObject) {
        em().detach(entityObject);
    }

    /**
     * Refresh from database, (optional) overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return returns <code>e</code> to be compliant with {@link IGDAO}
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity is not attached
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws javax.persistence.TransactionRequiredException see {@link EntityManager#refresh(Object)}
     * @see {@link EntityManager#refresh(Object)}
     */
    @Override
    public
    E refresh(E e) {
        em().refresh(e);
        return e;
    }

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Set property <em>javax.persistence.lock.timeout</em> to e.g. 5000 which means
     * 5 seconds if timeout elapsed too fast, or use another method with method parameter
     * {@link java.util.concurrent.TimeUnit}.
     *
     * @return returns <code>e</code> to be compliant with {@link IGDAO}
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity is not attached
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws javax.persistence.TransactionRequiredException see {@link EntityManager#refresh(Object, LockModeType)}
     * @throws javax.persistence.PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws javax.persistence.LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws javax.persistence.PersistenceException if an unsupported lock call
     *         is made
     * @see {@link EntityManager#refresh(Object, LockModeType)}
     */
    @Override
    public
    E refresh(E e, LockModeType lockMode) {
        em().refresh(e, lockMode);
        return e;
    }

    /**
     * Refresh from database overriding argument {@code e}.<p>
     * Use the timeout if the default one elapsed too fast.
     *
     * @return returns <code>e</code> to be compliant with {@link IGDAO}
     * @throws javax.persistence.EntityNotFoundException  entity reference does not exist in database
     * @throws IllegalArgumentException if {@code entityObject} is found not to be an entity
     *                                  or the entity is not attached
     * @throws IllegalStateException    if the entity manager has been closed, or
     *                                  the entity manager factory has been closed
     * @throws javax.persistence.TransactionRequiredException see {@link EntityManager#refresh(Object, LockModeType, Map)}
     * @throws javax.persistence.PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws javax.persistence.LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws javax.persistence.PersistenceException if an unsupported lock call
     *         is made
     * @see {@link EntityManager#refresh(Object, LockModeType, Map)}
     */
    @Override
    public
    E refresh(E e, LockModeType lockMode, TimeUnit timeoutUnits, long timeout) {
        em().refresh(e, lockMode, singletonMap("javax.persistence.lock.timeout", timeoutUnits.toMillis(timeout)));
        return e;
    }

    /**
     * @see javax.persistence.PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attribute was loaded
     */
    @Override
    public boolean isLoaded(@NotNull E entityObject, @NotNull String attributeName) {
        return em().getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entityObject, attributeName);
    }

    @Override
    public boolean isAttached(@NotNull E entityObject) {
        try {
            return em().contains(entityObject);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @Min(0)
    public long count() {
        return newQuery()
                .from(newQueryEntity())
                .fetchCount();
    }

    @Override
    @Min(0)
    public long count(@NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();
        JPAQuery<E> q = newQuery().from(entity);
        predicate.where(q, entity, alias(getEntityType(), entity));
        return q.fetchCount();
    }

    @Override
    @Min(0)
    public long count(@NotNull Function<E, Predicate> predicate) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicate.apply(alias(getEntityType(), entity)))
                .fetchCount();
    }

    /**
     * Persists the <tt>newInstance</tt> object into database.
     * Primary key is stored in <tt>newInstance</tt>.
     * Fail-Fast.
     */
    @Override
    public void save(@NotNull E newInstance) {
        em().persist(newInstance);
    }

    /**
     * Persists the <tt>newInstance</tt> object into database.
     * Primary key is stored in <tt>newInstance</tt>.
     * Fail-Fast.
     */
    @Override
    public void save(@NotNull Supplier<E> newInstance) {
        em().persist(newInstance.get());
    }

    /**
     * Merges given changes to the returned object.
     *
     * @param mergeFrom entity attached or detached to the persistence context. The entity object is returned detached.
     * @return newly merged entity, attached to the persistence context
     */
    @Override
    @NotNull
    public
    E merge(@NotNull E mergeFrom) {
        return em().merge(mergeFrom);
    }

    /**
     * This method is eligible for been intercepted in proxyable bean.
     * By proxying this method you can compare old entity with newly returned object in the interceptor.
     */
    @Override
    @NotNull
    public
    E merge(@NotNull Supplier<E> mergeFrom) {
        return em().merge(mergeFrom.get());
    }

    /**
     * Retrieves all objects that were previously persisted to the database.
     */
    @Override
    @NotNull
    public
    List<E> loadAll() {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .fetch();
    }

    @Override
    @NotNull
    public
    List<E> loadAll(@Min(0) int pagingOffset, @Min(1) int pageSize) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .offset(pagingOffset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    @NotNull
    public
    List<E> loadAll(@Min(0) int pagingOffset, @Min(1) int pageSize, @NotNull Collection<Predicate> predicates) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .offset(pagingOffset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    @NotNull
    public
    List<E> loadAll(@NotNull Collection<Predicate> predicates) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .fetch();
    }

    @Override
    @NotNull
    public
    List<E> loadAll(@Min(0) int pagingOffset, @Min(1) int pageSize, @NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQueryBase<E, ?> q = newQuery().from(entity);

        predicate.where(q, entity, alias(getEntityType(), entity));
        return q.offset(pagingOffset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    @NotNull
    public
    List<E> loadAll(@NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQuery<E> q = newQuery().from(entity);

        predicate.where(q, entity, alias(getEntityType(), entity));
        return q.fetch();
    }

    @Override
    @NotNull
    public
    List<E> loadAll(@NotNull BiConsumer<JPAQuery, E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQuery<E> q = newQuery().from(entity);

        predicate.accept(q, alias(getEntityType(), entity));
        return q.fetch();
    }

    @Override
    public List<E> loadAll(@NotNull Function<E, Predicate> predicate) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicate.apply(alias(getEntityType(), entity)))
                .fetch();
    }

    @Override
    public E load(@NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQuery<E> q = newQuery().from(entity);

        predicate.where(q, entity, alias(getEntityType(), entity));
        return q.fetchFirst();
    }

    @Override
    public E load(@NotNull BiConsumer<JPAQuery, E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQuery<E> q = newQuery().from(entity);

        predicate.accept(q, alias(getEntityType(), entity));
        return q.fetchFirst();
    }

    @Override
    public E load(@NotNull Function<E, Predicate> predicate) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicate.apply(alias(getEntityType(), entity)))
                .fetchFirst();
    }

    @Override
    public long delete(@NotNull BiConsumer<FilteredClause<?>, E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPADeleteClause q = newDeleteClause(entity);

        predicate.accept(q, alias(getEntityType(), entity));
        return q.execute();
    }

    /**
     * Remove an object from persistent storage in the database.
     *
     * @throws IllegalArgumentException     if the instance is not an
     *                                      entity or is a detached entity
     * @throws javax.persistence.TransactionRequiredException if invoked on a
     *                                      container-managed entity manager of type
     *                                      <code>PersistenceContextType.TRANSACTION</code> and there is
     *                                      no transaction
     */
    @Override
    public void delete(@NotNull E e) {
        em().remove(e);
    }

    @Override
    public long deleteAll() {
        return newDeleteClause(newQueryEntity()).execute();
    }

    @Override
    @Min(0)
    public
    int updateByNamedQuery(String sqlStatement) {
        return updateByNamedQuery(sqlStatement, new Object[0]);
    }

    @Override
    @Min(0)
    public
    int updateByNamedQuery(@NotNull String sqlStatement, Object... attributes) {
        return buildNamedQuery(Integer.class, sqlStatement, attributes)
                .executeUpdate();
    }

    @Override
    @Min(0)
    public
    int updateByNamedQuery(@Size String sqlStatement, @Size String attributeName, Object attributeValue) {
        return updateByNamedQuery(sqlStatement, singletonMap(attributeName, attributeValue));
    }

    @Override
    @Min(0)
    public
    int updateByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes) {
        return buildNamedQuery(Integer.class, sqlStatement, attributes)
                .executeUpdate();
    }

    @Override
    @NotNull
    public
    List<E> selectByNamedQuery(@NotNull String sqlStatement) {
        return selectByNamedQuery(sqlStatement, new Object[0]);
    }

    @Override
    @NotNull
    public
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass) {
        return selectByNamedQuery(sqlStatement, resultClass, new Object[0]);
    }

    @Override
    @NotNull
    public
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass,
                                   @Min(0) int paginationOffset, @Min(1) int pageSize) {
        return buildNamedQuery(resultClass, sqlStatement, new Object[0])
                .setFirstResult(paginationOffset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    @NotNull
    public
    List<E> selectByNamedQuery(@NotNull String sqlStatement, Object... attributes) {
        return selectByNamedQuery(sqlStatement, getEntityType(), attributes);
    }

    @Override
    @NotNull
    public
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass, Object... attributes) {
        return buildNamedQuery(resultClass, sqlStatement, attributes)
                .getResultList();
    }

    @Override
    @NotNull
    public
    List<E> selectByNamedQuery(@NotNull String sqlStatement, @NotNull String attributeName, Object attributeValue) {
        return selectByNamedQuery(sqlStatement, singletonMap(attributeName, attributeValue));
    }

    @Override
    @NotNull
    public
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull String attributeName,
                                   Object attributeValue, @NotNull Class<T> resultClass) {
        return selectByNamedQuery(sqlStatement, singletonMap(attributeName, attributeValue), resultClass);
    }

    @Override
    @NotNull
    public
    List<E> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes) {
        return selectByNamedQuery(sqlStatement, attributes, getEntityType());
    }

    @Override
    @NotNull
    public
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes,
                                   @NotNull Class<T> resultClass) {
        return buildNamedQuery(resultClass, sqlStatement, attributes)
                .getResultList();
    }

    @Override
    @NotNull
    public
    List<E> findByAttributeAsPattern(@NotNull String attributeName, @NotNull String pattern) {
        CriteriaBuilder b = em().getCriteriaBuilder();
        CriteriaQuery<E> c = b.createQuery(getEntityType());
        Root<E> selection = c.from(getEntityType());
        return em()
                .createQuery(c.select(selection).where(b.like(selection.<String>get(attributeName), pattern)))
                .getResultList();
    }

    @Override
    @NotNull
    public
    List<E> findByAttributeAsString(@NotNull String attributeName, @NotNull String attributeValue,
                                    boolean ignoreCase) {
        CriteriaBuilder b = em().getCriteriaBuilder();
        CriteriaQuery<E> c = b.createQuery(getEntityType());
        Root<E> selection = c.from(getEntityType());

        c = c.select(selection);

        if (attributeValue == null) {
            c = c.where(b.isNull(selection.<String>get(attributeName)));
        } else if (startsWith(attributeValue, '%', '_') || endsWith(attributeValue, '%', '_')) {
            // See SQL regex characters % _
            // http://www.objectdb.com/java/jpa/query/jpql/string#Criteria_Query_String_Expressions_

            StringBuilder phrase = new StringBuilder(attributeValue);

            if (startsWith(phrase, '%', '_')) {
                phrase.insert(0, '\\');
            }

            if (endsWith(phrase, '%', '_')) {
                phrase.insert(phrase.length() - 1, '\\');
            }

            c = c.where(ignoreCase
                    ? b.like(b.lower(selection.<String>get(attributeName)), attributeValue.toLowerCase())
                    : b.like(selection.<String>get(attributeName), attributeValue));
        } else {
            c = c.where(ignoreCase
                    ? b.like(b.lower(selection.<String>get(attributeName)), attributeValue.toLowerCase())
                    : b.like(selection.<String>get(attributeName), attributeValue));
        }

        return em().createQuery(c).getResultList();
    }

    private <T> TypedQuery<T> buildNamedQuery(@NotNull Class<T> resultClass, @NotNull String sqlStatement,
                                              @NotNull Map<String, ?> attributes) {
        TypedQuery<T> query = em().createNamedQuery(sqlStatement, resultClass);
        if (attributes != null) {
            attributes.entrySet()
                    .forEach(e -> query.setParameter(e.getKey(), e.getValue()));
        }
        return query;
    }

    private <T> TypedQuery<T> buildNamedQuery(@NotNull Class<T> resultClass, @NotNull String sqlStatement,
                                              @NotNull Object... attributes) {
        TypedQuery<T> query = em().createNamedQuery(sqlStatement, resultClass);
        if (attributes != null) {
            for (int i = 0; i < attributes.length; ++i) {
                query.setParameter(i + 1, attributes[i]);
            }
        }
        return query;
    }

    public static Map<Alias, PathBuilder<?>> mapPathBuilders(Collection<PathBuilder<?>> pathBuilders) {
        Map<Alias, PathBuilder<?>> map = new HashMap<>();
        for (PathBuilder<?> pathBuilder : pathBuilders) {
            map.put(new Alias(pathBuilder.getMetadata().getName()), pathBuilder);
        }
        return map;
    }

    public static Map<Alias, PathBuilder<?>> mapPathBuilders(PathBuilder<?>... pathBuilders) {
        return mapPathBuilders(Arrays.asList(pathBuilders));
    }

    protected static SQLTemplates findTemplates(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String dbProductName = metaData.getDatabaseProductName();
        String comparableProductName = dbProductName.trim().toLowerCase();
        switch (comparableProductName) {
            case "oracle":
            case "oracle database":
                // Oracle
                return new OracleTemplates();
            case "mysql":
                // MySQL
                return new MySQLTemplates();
            case "postgresql":
                // PostgreSQL
                return new PostgreSQLTemplates();
            case "apache derby":
                // Apache Derby
                return new DerbyTemplates();
            case "hsql database engine":
                // HSQL Database Engine
                return new HSQLDBTemplates();
            case "h2":
                // H2
                return new H2Templates();
            case "cubrid":
                // CUBRID
                return new CUBRIDTemplates();
            case "teradata":
                // Teradata
                return new TeradataTemplates();
            case "sqlite":
                // SQLite
                return new SQLiteTemplates();
            case "microsoft sql server":
            case "sql server":
                // Microsoft SQL Server
                return new SQLServerTemplates();
            case "microsoft sql server 2005":
            case "microsoft sql server 2008":
            case "microsoft sql server 2008 r2":
            case "sql server 2005":
            case "sql server 2008":
            case "sql server 2008 r2":
                // SQL Server 2005
                // Dialect for SQL Server 2005 and 2008.
                return new SQLServer2005Templates();
            case "microsoft sql server 2012":
            case "microsoft sql server 2014":
            case "sql server 2012":
            case "sql server 2014":
                // SQL Server 2012
                // Dialect for Microsoft SQL Server 2012 and later.
                return new SQLServer2012Templates();
            case "db2":
                // DB2
                return DEFAULT;
            case "db2zos":
                // DB2ZOS
                return DEFAULT;
            case "sybase":
                // Sybase
                return DEFAULT;
            case "ms jet": // (Access)
                // MS Jet
                return DEFAULT;
            default:
                return DEFAULT;
        }
    }

    private static boolean startsWith(CharSequence characters, char... anyOf) {
        if (characters.length() != 0) {
            for (char test : anyOf) {
                if (characters.charAt(0) == test) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean endsWith(CharSequence characters, char... anyOf) {
        if (characters.length() != 0) {
            int last = characters.length() - 1;
            for (char test : anyOf) {
                if (characters.charAt(last) == test) {
                    return true;
                }
            }
        }
        return false;
    }
}
