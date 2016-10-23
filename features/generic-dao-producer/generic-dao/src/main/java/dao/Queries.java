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

import javax.enterprise.inject.Vetoed;

@Vetoed
public final class Queries {
    private Queries() {
        throw new IllegalStateException("no instantiable constructor");
    }

    public static <Q, E1> Q $(GenericDaoWithoutId<E1> d1, I1<Query<E1>> q) {
        return q.$(q(d1));
    }

    public static <Q, E1, E2> Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2,
                                  I2<Query<E1>, Query<E2>> q) {
        return q.$(q(d1), q(d2));
    }

    public static <Q, E1, E2, E3> Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3,
                                      I3<Query<E1>, Query<E2>, Query<E3>> q) {
        return q.$(q(d1), q(d2), q(d3));
    }

    public static <Q, E1, E2, E3, E4> Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3,
                                          GenericDaoWithoutId<E4> d4,
                                          I4<Query<E1>, Query<E2>, Query<E3>, Query<E4>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4));
    }

    public static <Q, E1, E2, E3, E4, E5>
    Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3, GenericDaoWithoutId<E4> d4,
        GenericDaoWithoutId<E5> d5,
        I5<Query<E1>, Query<E2>, Query<E3>, Query<E4>, Query<E5>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4), q(d5));
    }

    public static <Q, E1, E2, E3, E4, E5, E6>
    Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3, GenericDaoWithoutId<E4> d4,
        GenericDaoWithoutId<E5> d5, GenericDaoWithoutId<E6> d6,
        I6<Query<E1>, Query<E2>, Query<E3>, Query<E4>, Query<E5>, Query<E6>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4), q(d5), q(d6));
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public static <Q, E1, E2, E3, E4, E5, E6, E7>
    Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3, GenericDaoWithoutId<E4> d4,
        GenericDaoWithoutId<E5> d5, GenericDaoWithoutId<E6> d6,
        GenericDaoWithoutId<E7> d7,
        I7<Query<E1>, Query<E2>, Query<E3>, Query<E4>, Query<E5>, Query<E6>, Query<E7>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4), q(d5), q(d6), q(d7));
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public static <Q, E1, E2, E3, E4, E5, E6, E7, E8>
    Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3, GenericDaoWithoutId<E4> d4,
        GenericDaoWithoutId<E5> d5, GenericDaoWithoutId<E6> d6, GenericDaoWithoutId<E7> d7, GenericDaoWithoutId<E8> d8,
        I8<Query<E1>, Query<E2>, Query<E3>, Query<E4>, Query<E5>, Query<E6>, Query<E7>, Query<E8>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4), q(d5), q(d6), q(d7), q(d8));
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public static <Q, E1, E2, E3, E4, E5, E6, E7, E8, E9>
    Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3, GenericDaoWithoutId<E4> d4,
        GenericDaoWithoutId<E5> d5, GenericDaoWithoutId<E6> d6, GenericDaoWithoutId<E7> d7, GenericDaoWithoutId<E8> d8,
        GenericDaoWithoutId<E9> d9,
        I9<Query<E1>, Query<E2>, Query<E3>, Query<E4>, Query<E5>, Query<E6>, Query<E7>, Query<E8>, Query<E9>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4), q(d5), q(d6), q(d7), q(d8), q(d9));
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public static <Q, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10>
    Q $(GenericDaoWithoutId<E1> d1, GenericDaoWithoutId<E2> d2, GenericDaoWithoutId<E3> d3, GenericDaoWithoutId<E4> d4,
        GenericDaoWithoutId<E5> d5, GenericDaoWithoutId<E6> d6, GenericDaoWithoutId<E7> d7, GenericDaoWithoutId<E8> d8,
        GenericDaoWithoutId<E9> d9, GenericDaoWithoutId<E10> d10,
        I10<Query<E1>, Query<E2>, Query<E3>, Query<E4>, Query<E5>, Query<E6>, Query<E7>, Query<E8>, Query<E9>,
                Query<E10>> q) {
        return q.$(q(d1), q(d2), q(d3), q(d4), q(d5), q(d6), q(d7), q(d8), q(d9), q(d10));
    }

    public interface I1<Q1> {
        <Q> Q $(Q1 q1);
    }

    public interface I2<Q1, Q2> {
        <Q> Q $(Q1 q1, Q2 q2);
    }

    public interface I3<Q1, Q2, Q3> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3);
    }

    public interface I4<Q1, Q2, Q3, Q4> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4);
    }

    public interface I5<Q1, Q2, Q3, Q4, Q5> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4, Q5 q5);
    }

    public interface I6<Q1, Q2, Q3, Q4, Q5, Q6> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4, Q5 q5, Q6 q6);
    }

    public interface I7<Q1, Q2, Q3, Q4, Q5, Q6, Q7> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4, Q5 q5, Q6 q6, Q7 q7);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public interface I8<Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4, Q5 q5, Q6 q6, Q7 q7, Q8 q8);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public interface I9<Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4, Q5 q5, Q6 q6, Q7 q7, Q8 q8, Q9 q9);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public interface I10<Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10> {
        <Q> Q $(Q1 q1, Q2 q2, Q3 q3, Q4 q4, Q5 q5, Q6 q6, Q7 q7, Q8 q8, Q9 q9, Q10 q10);
    }

    @SuppressWarnings("checkstyle")
    private static <E> Query<E> q(DaoWithoutId<E> dao) {
        return ((GenericDaoWithoutId<E>) dao).createQuery();
    }
}
