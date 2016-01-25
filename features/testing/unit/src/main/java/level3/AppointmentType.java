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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for appointmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="appointmentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="link" type="{}linkType"/>
 *         &lt;element name="patient" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="doctor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="openSince" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="openTill" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "appointmentType", propOrder = {
    "link",
    "patient",
    "doctor",
    "openSince",
    "openTill"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class AppointmentType {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected LinkType link;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String patient;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String doctor;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected XMLGregorianCalendar openSince;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected XMLGregorianCalendar openTill;
    @XmlAttribute(name = "id")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Long id;

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link LinkType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public LinkType getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setLink(LinkType value) {
        this.link = value;
    }

    /**
     * Gets the value of the patient property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getPatient() {
        return patient;
    }

    /**
     * Sets the value of the patient property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setPatient(String value) {
        this.patient = value;
    }

    /**
     * Gets the value of the doctor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getDoctor() {
        return doctor;
    }

    /**
     * Sets the value of the doctor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setDoctor(String value) {
        this.doctor = value;
    }

    /**
     * Gets the value of the openSince property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public XMLGregorianCalendar getOpenSince() {
        return openSince;
    }

    /**
     * Sets the value of the openSince property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setOpenSince(XMLGregorianCalendar value) {
        this.openSince = value;
    }

    /**
     * Gets the value of the openTill property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public XMLGregorianCalendar getOpenTill() {
        return openTill;
    }

    /**
     * Sets the value of the openTill property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setOpenTill(XMLGregorianCalendar value) {
        this.openTill = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2016-01-24T09:41:36+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setId(Long value) {
        this.id = value;
    }

}
