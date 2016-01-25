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
package level3;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="relationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="about"/>
 *     &lt;enumeration value="appendix"/>
 *     &lt;enumeration value="canonical"/>
 *     &lt;enumeration value="collection"/>
 *     &lt;enumeration value="current"/>
 *     &lt;enumeration value="first"/>
 *     &lt;enumeration value="help"/>
 *     &lt;enumeration value="last"/>
 *     &lt;enumeration value="latest-version"/>
 *     &lt;enumeration value="license"/>
 *     &lt;enumeration value="next"/>
 *     &lt;enumeration value="predecessor-version"/>
 *     &lt;enumeration value="previous"/>
 *     &lt;enumeration value="self"/>
 *     &lt;enumeration value="start"/>
 *     &lt;enumeration value="successor-version"/>
 *     &lt;enumeration value="tag"/>
 *     &lt;enumeration value="version-history"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "relationType")
@XmlEnum
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public enum RelationType {

    @XmlEnumValue("about")
    ABOUT("about"),
    @XmlEnumValue("appendix")
    APPENDIX("appendix"),
    @XmlEnumValue("canonical")
    CANONICAL("canonical"),
    @XmlEnumValue("collection")
    COLLECTION("collection"),
    @XmlEnumValue("current")
    CURRENT("current"),
    @XmlEnumValue("first")
    FIRST("first"),
    @XmlEnumValue("help")
    HELP("help"),
    @XmlEnumValue("last")
    LAST("last"),
    @XmlEnumValue("latest-version")
    LATEST_VERSION("latest-version"),
    @XmlEnumValue("license")
    LICENSE("license"),
    @XmlEnumValue("next")
    NEXT("next"),
    @XmlEnumValue("predecessor-version")
    PREDECESSOR_VERSION("predecessor-version"),
    @XmlEnumValue("previous")
    PREVIOUS("previous"),
    @XmlEnumValue("self")
    SELF("self"),
    @XmlEnumValue("start")
    START("start"),
    @XmlEnumValue("successor-version")
    SUCCESSOR_VERSION("successor-version"),
    @XmlEnumValue("tag")
    TAG("tag"),
    @XmlEnumValue("version-history")
    VERSION_HISTORY("version-history");
    private final String value;

    RelationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelationType fromValue(String v) {
        for (RelationType c: RelationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
