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
package javaee.samples.frameworks.junitjparule.spi;

import javaee.samples.frameworks.junitjparule.jms.JMSContextMock;
import javax.inject.Inject;
import javax.jms.*;
import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javaee.samples.frameworks.junitjparule.spi.JMSResourceCtx.CTX;

public class JMSContextInjectionPoint implements InjectionPoint<Inject> {

    @Override
    public Class<Inject> getAnnotationType() {
        return Inject.class;
    }

    @Override
    public <T> Optional<Object> lookupOf(Class<?> declaredInjectionType, Inject injectionAnnotation, T bean, Class<? extends T> beanType) {
        if (declaredInjectionType == JMSContext.class) {
            if (!CTX.hasJMSContext()) {
                ConnectionFactory factory = CTX.startConnectionFactory();
                CTX.setJmsContext(new JMSContextMock(factory));
            }
            return of(CTX.getJmsContext());
        }
        return empty();
    }

    @Override
    public void destroy() {
    }
}
