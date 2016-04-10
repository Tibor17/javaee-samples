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
package javaee.samples.frameworks.injection.injections.gconstructor;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class C extends SuperC<A, B> {
    private final A a;
    private final B b;

    public @Inject C(@NotNull A a, B b) {
        super(a, b);
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return a.toString() + b;
    }
}
