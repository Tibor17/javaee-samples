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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Util {
    private Util() {
        throw new IllegalStateException("no instantiable constructor");
    }

    public static Map<String, Object> resolveMapMessage(MapMessage mapMessage) throws JMSException {
        Map<String, Object> map = new LinkedHashMap<>();
        for (@SuppressWarnings({"unchecked", "checkstyle:emptyforiteratorpad"})
             Enumeration<String> e = mapMessage.getMapNames(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            map.put(key, mapMessage.getObject(key));
        }
        return map;
    }

    public  static  <T> T castTo(Class<T> c, Message message) throws JMSException {
        if (message == null) {
            throw new MessageFormatRuntimeException("empty body");
        }

        if (!isBodyAssignableTo(message, c)) {
            throw new MessageFormatRuntimeException("the message body cannot be assigned to the specified type "
                    + c
                    + " and the Message type "
                    + StreamMessage.class
                    + " cannot be used");
        }

        if (message instanceof ObjectMessage) {
            return c.cast(((ObjectMessage) message).getObject());
        } else if (message instanceof MapMessage) {
            return c.cast(resolveMapMessage((MapMessage) message));
        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            bytesMessage.reset();
            try {
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = bytesMessage.readByte();
                }
                return c.cast(bytes);
            } finally {
                bytesMessage.reset();
            }
        } else if (message instanceof TextMessage) {
            return c.cast(((TextMessage) message).getText());
        } else {
            throw new MessageFormatRuntimeException("internal error. unknown message type "
                    + message.getClass() + " or empty body");
        }
    }

    public static boolean isBodyAssignableTo(Message message, Class<?> c) throws JMSException {
        if (message instanceof ObjectMessage) {
            return Serializable.class.isAssignableFrom(c);
        } else if (message instanceof MapMessage) {
            return c != Map.class && c != Object.class;
        } else if (message instanceof BytesMessage) {
            return c != byte[].class && c != Object.class;
        } else if (message instanceof TextMessage) {
            return c.isAssignableFrom(String.class);
        } else {
            return !(message instanceof StreamMessage);
        }
    }
}
