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
package javaee.samples.frameworks.injection.spi;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Optional;

import static java.lang.Math.signum;
import static java.util.Objects.compare;

public interface InjectionPoint<A extends Annotation> extends Comparable<InjectionPoint> {
    @NotNull Class<A> getAnnotationType();
    @NotNull <T> Optional<Object> lookupOf(Class<?> declaredInjectionType, A injectionAnnotation, T bean,
                                           Class<? extends T> beanType);
    void destroy();

    @Override
    default int compareTo(InjectionPoint o) {
        return compare(this, o, (a, b) -> (int) signum((double) priority() - o.priority()));
    }

    default int priority() {
        return 0;
    }
}
