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

import com.querydsl.jpa.JPAQueryBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

import javax.persistence.*;
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
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.querydsl.core.alias.Alias.alias;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

public abstract class GenericDAO<E, PK extends Number & Comparable<PK>> implements IDAO<E,PK> {
    private final Class<E> entityType;
    private final Class<PK> primaryKeyType;

    @SuppressWarnings("unchecked")
    protected GenericDAO() {
        ParameterizedType parameterizedType = null;
        for (Class<?> parent = getClass(); parent != GenericDAO.class; parent = parent.getSuperclass()) {
            Type type = parent.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType) type;
                break;
            }
        }
        Type[] genericTypes = requireNonNull(parameterizedType, "java generic types missing").getActualTypeArguments();
        entityType = (Class<E>) genericTypes[0];
        primaryKeyType = (Class<PK>) genericTypes[1];
    }

    protected GenericDAO(Class<E> entityType, Class<PK> primaryKeyType) {
        this.entityType = entityType;
        this.primaryKeyType = primaryKeyType;
    }

    protected abstract
    @NotNull
    EntityManager em();

    protected
    @NotNull
    Class<E> getEntityType() {
        return entityType;
    }

    protected
    @NotNull
    Class<PK> getPrimaryKeyType() {
        return primaryKeyType;
    }

    protected
    @NotNull
    PathBuilder<E> newQueryEntity() {
        StringBuilder variable = new StringBuilder(entityType.getSimpleName());
        char firstLetter = variable.charAt(0);
        variable.setCharAt(0, Character.toLowerCase(firstLetter));
        return newQueryEntity(variable.toString());
    }

    protected
    @NotNull
    PathBuilder<E> newQueryEntity(@NotNull @Size(min = 1) String alias) {
        return new PathBuilder<E>(entityType, alias);
    }

    protected
    @NotNull
    JPAQuery<E> newQuery() {
        return new JPAQuery<E>(em());
    }

    protected
    @NotNull
    JPAQuery newSubQuery() {
        return new JPAQuery();
    }

    protected
    @NotNull
    JPADeleteClause newDeleteClause(@NotNull EntityPath<?> q) {
        return new JPADeleteClause(em(), q);
    }

    protected
    @NotNull
    JPAUpdateClause newUpdateClause(@NotNull EntityPath<?> q) {
        return new JPAUpdateClause(em(), q);
    }

    protected
    @NotNull
    SQLInsertClause newSQLInsertClause(@NotNull Connection connection, @NotNull SQLTemplates templates, @NotNull RelationalPath<?> q) throws SQLException {
        return new SQLInsertClause(connection, templates, q);
    }

    protected
    @NotNull
    SQLInsertClause newSQLInsertClause(@NotNull Connection connection, @NotNull RelationalPath<?> q) throws SQLException {
        return new SQLInsertClause(connection, findTemplates(connection), q);
    }

    protected
    @NotNull
    SQLInsertClause newSQLInsertClause(@NotNull RelationalPath<?> q) throws SQLException {
        EntityManager em = em();
        // JPQLTemplates templates = JPAProvider.getTemplates(em);
        Connection connection = em.unwrap(Connection.class);
        return new SQLInsertClause(connection, findTemplates(connection), q);
    }

    protected
    @NotNull
    SQLQuery newSQLQuery(@NotNull Connection connection, @NotNull SQLTemplates templates) {
        return new SQLQuery(connection, templates);
    }

    protected
    @NotNull
    SQLQuery newSQLQuery(@NotNull SQLTemplates templates) {
        return new SQLQuery(templates);
    }

    /**
     * @see PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attributes are loaded
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
    @Override
    public
    @NotNull
    E reload(@NotNull E entityObject) {
        PK id;
        if (!hasId(entityObject) || (id = getIdentifier(entityObject)) == null) {
            throw new IllegalArgumentException("does not have id");
        }
        E e = load(id);
        if (e == null) {
            throw new EntityNotFoundException(entityType.getSimpleName() + " record with " + id + " does not exist in database");
        }
        return e;
    }

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
    @Override
    public
    @NotNull
    E reloadIfDetached(@NotNull E entityObject) {
        return hasId(entityObject) && !isAttached(entityObject) ? reload(entityObject) : entityObject;
    }

    /**
     * Behaves the same as {@link EntityManager#getReference EntityManager.getReference}.
     */
    @Override
    public
    @NotNull
    E fetchEager(@NotNull E entityObject) {
        return em().getReference(entityType, entityObject);
    }

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
    @Override
    public
    @NotNull
    E refresh(@NotNull E e) {
        e = reloadIfDetached(e);
        em().refresh(e);
        return e;
    }

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
    @Override
    public
    @NotNull
    E refresh(@NotNull E e, @NotNull LockModeType lockMode) {
        e = reload(e);
        em().refresh(e, lockMode);
        return e;
    }

    /**
     * @see PersistenceUnitUtil#isLoaded(Object) if all <code>FetchType.EAGER</code> attribute was loaded
     */
    @Override
    public boolean isLoaded(@NotNull E entityObject, @NotNull String attributeName) {
        return em().getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(entityObject, attributeName);
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
        return id != null && id.longValue() > 0;
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
    public long count() {
        return newQuery()
                .from(newQueryEntity())
                .fetchCount();
    }

    @Override
    public long count(Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();
        JPAQuery<E> q = newQuery().from(entity);
        predicate.where(q, entity, alias(entityType, entity));
        return q.fetchCount();
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
     * Merges given changes to the returned object.
     *
     * @param mergeFrom entity attached or detached to the persistence context. The entity object is returned detached.
     * @return newly merged entity, attached to the persistence context
     */
    @Override
    public
    @NotNull
    E merge(@NotNull E mergeFrom) {
        return em().merge(mergeFrom);
    }

    /**
     * This method is eligible for been intercepted in proxyable bean.
     * By proxying this method you can compare old entity with newly returned object in the interceptor.
     */
    @Override
    public
    @NotNull
    E merge(@NotNull Supplier<E> mergeFrom) {
        return em().merge(mergeFrom.get());
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
        return em().getReference(entityType, id);
    }

    /**
     * Retrieves an object that was previously persisted to the database
     * using the indicated id as primary key.
     */
    @Override
    public E load(@NotNull PK id) {
        return em().find(entityType, id);
    }

    @Override
    public E load(@NotNull PK id, @NotNull LockModeType lock) {
        return em().find(entityType, id, lock);
    }

    /**
     * Retrieves all objects that were previously persisted to the database.
     */
    @Override
    public
    @NotNull
    List<E> loadAll() {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .fetch();
    }

    @Override
    public
    @NotNull
    List<E> loadAll(@Size int page, @Size(min = 1) int pageSize) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .offset(page * pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public
    @NotNull
    List<E> loadAll(@Size int page, @Size(min = 1) int pageSize, @NotNull Collection<Predicate> predicates) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .offset(page * pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public
    @NotNull
    List<E> loadAll(@NotNull Collection<Predicate> predicates) {
        PathBuilder<E> entity = newQueryEntity();
        return newQuery()
                .from(entity)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .fetch();
    }

    @Override
    public
    @NotNull
    List<E> loadAll(@Size int page, @Size(min = 1) int pageSize, @NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQueryBase<E, ?> q = newQuery().from(entity);

        predicate.where(q, entity, alias(entityType, entity));
        return q.offset(page * pageSize)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public
    @NotNull
    List<E> loadAll(@NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQuery<E> q = newQuery().from(entity);

        predicate.where(q, entity, alias(entityType, entity));
        return q.fetch();
    }

    @Override
    public E load(@NotNull Where<E> predicate) {
        PathBuilder<E> entity = newQueryEntity();

        JPAQuery<E> q = newQuery().from(entity);

        predicate.where(q, entity, alias(entityType, entity));
        return q.fetchFirst();
    }

    /**
     * Remove an object from persistent storage in the database.
     */
    @Override
    public void delete(@NotNull PK id) {
        em().remove(load(id));
    }

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
    @Override
    public void delete(@NotNull E e) {
        em().remove(e);
    }

    @Override
    public long deleteAll() {
        return newDeleteClause(newQueryEntity()).execute();
    }

    @Override
    public
    @Min(0)
    int updateByNamedQuery(String sqlStatement) {
        return updateByNamedQuery(sqlStatement, new Object[0]);
    }

    @Override
    public
    @Min(0)
    int updateByNamedQuery(@NotNull String sqlStatement, Object... attributes) {
        return buildNamedQuery(Integer.class, sqlStatement, attributes)
                .executeUpdate();
    }

    @Override
    public
    @Min(0)
    int updateByNamedQuery(@Size String sqlStatement, @Size String attributeName, Object attributeValue) {
        return updateByNamedQuery(sqlStatement, singletonMap(attributeName, attributeValue));
    }

    @Override
    public
    @Min(0)
    int updateByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes) {
        return buildNamedQuery(Integer.class, sqlStatement, attributes)
                .executeUpdate();
    }

    @Override
    public
    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement) {
        return selectByNamedQuery(sqlStatement, new Object[0]);
    }

    @Override
    public
    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass) {
        return selectByNamedQuery(sqlStatement, resultClass, new Object[0]);
    }

    @Override
    public
    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement, Object... attributes) {
        return selectByNamedQuery(sqlStatement, entityType, attributes);
    }

    @Override
    public
    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Class<T> resultClass, Object... attributes) {
        return buildNamedQuery(resultClass, sqlStatement, attributes)
                .getResultList();
    }

    @Override
    public
    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement, @NotNull String attributeName, Object attributeValue) {
        return selectByNamedQuery(sqlStatement, singletonMap(attributeName, attributeValue));
    }

    @Override
    public
    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull String attributeName, Object attributeValue, @NotNull Class<T> resultClass) {
        return selectByNamedQuery(sqlStatement, singletonMap(attributeName, attributeValue), resultClass);
    }

    @Override
    public
    @NotNull
    List<E> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes) {
        return selectByNamedQuery(sqlStatement, attributes, entityType);
    }

    @Override
    public
    @NotNull
    <T> List<T> selectByNamedQuery(@NotNull String sqlStatement, @NotNull Map<String, ?> attributes, @NotNull Class<T> resultClass) {
        return buildNamedQuery(resultClass, sqlStatement, attributes)
                .getResultList();
    }

    @Override
    public
    @NotNull
    List<E> findByAttributeAsPattern(@NotNull String attributeName, @NotNull String pattern) {
        CriteriaBuilder b = em().getCriteriaBuilder();
        CriteriaQuery<E> c = b.createQuery(entityType);
        Root<E> selection = c.from(entityType);
        return em()
                .createQuery(c.select(selection).where(b.like(selection.<String>get(attributeName), pattern)))
                .getResultList();
    }

    @Override
    public
    @NotNull
    List<E> findByAttributeAsString(@NotNull String attributeName, @NotNull String attributeValue, boolean ignoreCase) {
        CriteriaBuilder b = em().getCriteriaBuilder();
        CriteriaQuery<E> c = b.createQuery(entityType);
        Root<E> selection = c.from(entityType);

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

            c = c.where(ignoreCase ? b.like(b.lower(selection.<String>get(attributeName)), attributeValue.toLowerCase()) : b.like(selection.<String>get(attributeName), attributeValue));
        } else {
            c = c.where(ignoreCase ? b.like(b.lower(selection.<String>get(attributeName)), attributeValue.toLowerCase()) : b.like(selection.<String>get(attributeName), attributeValue));
        }

        return em().createQuery(c).getResultList();
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
                return SQLTemplates.DEFAULT;
            case "db2zos":
                // DB2ZOS
                return SQLTemplates.DEFAULT;
            case "sybase":
                // Sybase
                return SQLTemplates.DEFAULT;
            case "ms jet": // (Access)
                // MS Jet
                return SQLTemplates.DEFAULT;
            default:
                return SQLTemplates.DEFAULT;
        }
    }

    private <T> TypedQuery<T> buildNamedQuery(@NotNull Class<T> resultClass, @NotNull String sqlStatement, @NotNull Map<String, ?> attributes) {
        TypedQuery<T> query = em().createNamedQuery(sqlStatement, resultClass);
        if (attributes != null) {
            attributes.entrySet()
                    .forEach(e -> query.setParameter(e.getKey(), e.getValue()));
        }
        return query;
    }

    private <T> TypedQuery<T> buildNamedQuery(@NotNull Class<T> resultClass, @NotNull String sqlStatement, @NotNull Object... attributes) {
        TypedQuery<T> query = em().createNamedQuery(sqlStatement, resultClass);
        if (attributes != null) {
            for (int i = 0; i < attributes.length; ++i) {
                query.setParameter(i + 1, attributes[i]);
            }
        }
        return query;
    }
}
