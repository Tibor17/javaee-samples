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
package audit.query.search.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for null.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType>
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SORT_BY_INITIATOR"/>
 *     &lt;enumeration value="SORT_BY_OPERATION_KEY"/>
 *     &lt;enumeration value="SORT_BY_MODULE"/>
 *     &lt;enumeration value="SORT_BY_DESCRIPTION"/>
 *     &lt;enumeration value="SORT_BY_DATE"/>
 *     &lt;enumeration value="SORT_BY_DEFAULT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "")
@XmlEnum
public enum SortField {

    SORT_BY_INITIATOR {
        @Override
        public String fieldName() {
            return "initiator";
        }
    },
    SORT_BY_OPERATION_KEY {
        @Override
        public String fieldName() {
            return "operationKey";
        }
    },
    SORT_BY_MODULE {
        @Override
        public String fieldName() {
            return "module";
        }
    },
    SORT_BY_DESCRIPTION {
        @Override
        public String fieldName() {
            return "description";
        }
    },
    SORT_BY_DATE {
        @Override
        public String fieldName() {
            return "##date";
        }
    },
    SORT_BY_DEFAULT {
        @Override
        public String fieldName() {
            return "id";
        }
    };

    public abstract String fieldName();

    public String value() {
        return name();
    }

    public static SortField fromValue(String v) {
        return valueOf(v);
    }

}
