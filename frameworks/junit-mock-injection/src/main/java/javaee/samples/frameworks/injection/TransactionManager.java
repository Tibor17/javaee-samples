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

final class TransactionManager {
    private static class TransactionBarrier {
        private int barriers;
    }

    private static final ThreadLocal<TransactionBarrier> CURRENT = new ThreadLocal<TransactionBarrier>() {
        @Override
        protected TransactionBarrier initialValue() {
            return new TransactionBarrier();
        }
    };

    private TransactionManager() {
        throw new IllegalStateException("not instantiable constructor");
    }

    @SuppressWarnings("unused")
    static int countTransactionBarriers() {
        return CURRENT.get().barriers;
    }

    static boolean canStartOrCloseTransaction() {
        return CURRENT.get().barriers == 0;
    }

    static void increaseTransactionBarriers() {
        ++CURRENT.get().barriers;
    }

    static void decreaseTransactionBarriers() {
        --CURRENT.get().barriers;
    }

    static void clean() {
        CURRENT.remove();
    }
}
