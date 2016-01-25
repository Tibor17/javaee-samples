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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;


/**
 * 
 *                 Richardson Maturity Model
 *                 Level 3 - Hypermedia Controls
 *                 Martin Fowler: article
 *                 {@link http://martinfowler.com/articles/richardsonMaturityModel.html}
 *                 Link Relations Specification:
 *                 {@link http://www.iana.org/assignments/link-relations/link-relations.xhtml}
 *             
 * 
 * <p>Java class for linkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="linkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="rel" type="{}relationType" use="{}required" />
 *       &lt;attribute name="uri" type="{http://www.w3.org/2001/XMLSchema}anyURI" use="{}required" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkType")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class LinkType {

    @XmlAttribute(name = "rel", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected RelationType rel;
    @XmlAttribute(name = "uri", required = true)
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    @XmlJavaTypeAdapter(UriAdapter.class)
    protected URI uri;

    /**
     * Gets the value of the rel property.
     * 
     * @return
     *     possible object is
     *     {@link RelationType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public RelationType getRel() {
        return rel;
    }

    /**
     * Sets the value of the rel property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setRel(RelationType value) {
        this.rel = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link URI }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link URI }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setUri(URI value) {
        this.uri = value;
    }

}
