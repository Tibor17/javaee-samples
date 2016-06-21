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
package javaee.samples.frameworks.injection.jms;

import javax.jms.*;

import static java.lang.reflect.Proxy.newProxyInstance;
import static javaee.samples.frameworks.injection.jms.Util.castTo;
import static javaee.samples.frameworks.injection.jms.Util.isBodyAssignableTo;

public final class Jms20MessageBuilder<T extends Message> {
    private final Class<T> proxyType;

    public Jms20MessageBuilder(Class<T> proxyType) {
        this.proxyType = proxyType;
    }

    public T buildJms20Message(Message message) {
        Object proxyMsg = newProxyInstance(context(), new Class<?>[]{proxyType}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getJMSDeliveryTime":
                case "setJMSDeliveryTime":
                    throw new JMSException("not implemented");
                case "getBody":
                    return castTo((Class<?>) args[0], message);
                case "isBodyAssignableTo":
                    return isBodyAssignableTo(message, (Class<?>) args[0]);
                default:
                    return method.invoke(message, args);
            }
        });
        return proxyType.cast(proxyMsg);
    }

    public static Message proxyJms20Message(Message message) {
        final Class<? extends Message> proxyType = resolveToMessageClass(message);
        return new Jms20MessageBuilder<>(proxyType)
                .buildJms20Message(message);
    }

    private static ClassLoader context() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static Class<? extends Message> resolveToMessageClass(Message message) {
        if (message instanceof BytesMessage) return BytesMessage.class;
        else if (message instanceof MapMessage) return MapMessage.class;
        else if (message instanceof ObjectMessage) return ObjectMessage.class;
        else if (message instanceof StreamMessage) return StreamMessage.class;
        else if (message instanceof TextMessage) return TextMessage.class;
        else return Message.class;
    }
}
