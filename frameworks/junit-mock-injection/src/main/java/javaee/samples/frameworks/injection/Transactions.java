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
package javaee.samples.frameworks.injection;

import javax.persistence.EntityManager;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static javaee.samples.frameworks.injection.JPARule.CURRENT_JPA_RULE;
import static java.util.Objects.requireNonNull;

public final class Transactions {

    private Transactions() {
        throw new IllegalStateException("cannot instantiate constructor");
    }

    public static void $(Consumer<EntityManager> block) {
        getCurrentRule().$(block);
    }

    public static <T> void $(Consumer<T> block, T o) {
        $(() -> block.accept(o));
    }

    public static <T, R> R $$(Function<T, R> block, T o) {
        return $(() -> block.apply(o));
    }

    public static <T, U, R> R $(BiFunction<T, U, R> block, T t, U u) {
        return $(() -> block.apply(t, u));
    }

    public static <T, U, S, R> R $(TripleFunction<T, U, S, R> block, T t, U u, S s) {
        return $(() -> block.apply(t, u, s));
    }

    public static <S, T, U, O, R> R $(QuadFunction<S, T, U, O, R> block, S s, T t, U u, O o) {
        return $(() -> block.apply(s, t, u, o));
    }

    public static <S, T, U, O, W, R> R $(FiveFunction<S, T, U, O, W, R> block, S s, T t, U u, O o, W w) {
        return $(() -> block.apply(s, t, u, o, w));
    }

    public static <T> T $(Supplier<T> block) {
        return getCurrentRule().$(block);
    }

    public static void $(Runnable block) {
        getCurrentRule().$(block);
    }

    public static <T> T $$(Function<EntityManager, T> block) {
        return getCurrentRule().$$(block);
    }

    private static JPARule getCurrentRule() {
        return requireNonNull(CURRENT_JPA_RULE.get(), "@PersistenceContext(unitName = ...) or JpaJUnitRule should be used");
    }
}
