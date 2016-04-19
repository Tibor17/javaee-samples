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

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

import java.io.*;
import java.util.GregorianCalendar;

import static audit.util.Dates.format;
import static audit.util.Dates.toXMLGregorianCalendar;
import static javax.xml.datatype.DatatypeFactory.newInstance;
import static org.jvnet.jaxb2_commons.locator.util.LocatorUtils.property;


/**
 * <p>Java class for AuditQuery complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AuditQuery">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="initiator" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="module" type="{}moduleType"/>
 *         &lt;element name="operationKey" type="{}operationKeyType"/>
 *         &lt;element name="description" type="{}descriptionType"/>
 *         &lt;element name="from" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="to" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="startRowNum" type="{}startRowNumType"/>
 *         &lt;element name="pageSize" type="{}pageSizeType"/>
 *         &lt;element ref="{}sortField"/>
 *         &lt;element name="searchAnyError" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditQuery", propOrder = {
    "initiator",
    "module",
    "operationKey",
    "description",
    "from",
    "to",
    "startRowNum",
    "pageSize",
    "sortField",
    "searchAnyError",
    "error"
})
public class AuditQuery
        implements Paging, Serializable, Equals, HashCode {

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("initiator", Long.TYPE),
            new ObjectStreamField("module", String.class),
            new ObjectStreamField("operationKey", String.class),
            new ObjectStreamField("description", String.class),
            new ObjectStreamField("startRowNum", Integer.class),
            new ObjectStreamField("pageSize", Integer.class),
            new ObjectStreamField("sortField", Enum.class),
            new ObjectStreamField("searchAnyError", String.class),
            new ObjectStreamField("error", String.class),
    };

    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long initiator;
    @XmlElement(required = true, nillable = true)
    @Size(min = 1, max = 31)
    protected String module;
    @XmlElement(required = true, nillable = true)
    @Size(min = 1, max = 255)
    protected String operationKey;
    @XmlElement(required = true, nillable = true)
    @Size(min = 1, max = 255)
    protected String description;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar from;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar to;
    @javax.validation.constraints.NotNull
    @javax.validation.constraints.DecimalMin("0")
    protected java.lang.Integer startRowNum;
    @javax.validation.constraints.NotNull
    @javax.validation.constraints.DecimalMin("1")
    protected java.lang.Integer pageSize;
    @XmlElement(required = true, defaultValue = "SORT_BY_DEFAULT")
    @javax.validation.constraints.NotNull
    protected SortField sortField;
    @XmlElement(defaultValue = "false")
    @javax.validation.constraints.NotNull
    protected Boolean searchAnyError;
    @XmlElement(required = true, nillable = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String error;

    /**
     * Gets the value of the initiator property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getInitiator() {
        return initiator;
    }

    /**
     * Sets the value of the initiator property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setInitiator(Long value) {
        this.initiator = value;
    }

    public boolean isSetInitiator() {
        return (this.initiator!= null);
    }

    /**
     * Gets the value of the module property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the value of the module property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setModule(String value) {
        this.module = value;
    }

    public boolean isSetModule() {
        return (this.module!= null);
    }

    /**
     * Gets the value of the operationKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOperationKey() {
        return operationKey;
    }

    /**
     * Sets the value of the operationKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOperationKey(String value) {
        this.operationKey = value;
    }

    public boolean isSetOperationKey() {
        return (this.operationKey!= null);
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    public boolean isSetDescription() {
        return (this.description!= null);
    }

    /**
     * Gets the value of the from property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setFrom(XMLGregorianCalendar value) {
        this.from = value;
    }

    public void setFrom(GregorianCalendar value) {
        try {
            from = newInstance().newXMLGregorianCalendar(value);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    public boolean isSetFrom() {
        return (this.from!= null);
    }

    /**
     * Gets the value of the to property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setTo(XMLGregorianCalendar value) {
        this.to = value;
    }

    public void setTo(GregorianCalendar value) {
        try {
            to = newInstance().newXMLGregorianCalendar(value);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    public boolean isSetTo() {
        return (this.to!= null);
    }

    /**
     * Gets the value of the startRowNum property.
     *
     */
    public int getStartRowNum() {
        return startRowNum;
    }

    /**
     * Sets the value of the startRowNum property.
     *
     */
    public AuditQuery setStartRowNum(int value) {
        this.startRowNum = value;
        return this;
    }

    public boolean isSetStartRowNum() {
        return true;
    }

    /**
     * Gets the value of the pageSize property.
     *
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the value of the pageSize property.
     *
     */
    public AuditQuery setPageSize(@Min(0) int startRowNum) {
        this.pageSize = startRowNum;
        return this;
    }

    public boolean isSetPageSize() {
        return true;
    }

    /**
     * Gets the value of the sortField property.
     *
     * @return
     *     possible object is
     *     {@link SortField }
     *
     */
    public SortField getSortField() {
        return sortField;
    }

    /**
     * Sets the value of the sortField property.
     *
     * @param value
     *     allowed object is
     *     {@link SortField }
     *
     */
    public void setSortField(SortField value) {
        this.sortField = value;
    }

    public boolean isSetSortField() {
        return (this.sortField!= null);
    }

    /**
     * Gets the value of the searchAnyError property.
     *
     */
    public Boolean isSearchAnyError() {
        return searchAnyError;
    }

    /**
     * Sets the value of the searchAnyError property.
     *
     */
    public void setSearchAnyError(Boolean value) {
        this.searchAnyError = value;
    }

    public boolean isSetSearchAnyError() {
        return true;
    }

    /**
     * Gets the value of the error property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setError(String value) {
        this.error = value;
    }

    public boolean isSetError() {
        return (this.error!= null);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy strategy) {
        int currentHashCode = 1;
        {
            Long theInitiator;
            theInitiator = this.getInitiator();
            currentHashCode = strategy.hashCode(property(locator, "initiator", theInitiator), currentHashCode, theInitiator);
        }
        {
            String theModule;
            theModule = this.getModule();
            currentHashCode = strategy.hashCode(property(locator, "module", theModule), currentHashCode, theModule);
        }
        {
            String theOperationKey;
            theOperationKey = this.getOperationKey();
            currentHashCode = strategy.hashCode(property(locator, "operationKey", theOperationKey), currentHashCode, theOperationKey);
        }
        {
            String theDescription;
            theDescription = this.getDescription();
            currentHashCode = strategy.hashCode(property(locator, "description", theDescription), currentHashCode, theDescription);
        }
        {
            XMLGregorianCalendar theFrom;
            theFrom = this.getFrom();
            currentHashCode = strategy.hashCode(property(locator, "from", theFrom), currentHashCode, theFrom);
        }
        {
            XMLGregorianCalendar theTo;
            theTo = this.getTo();
            currentHashCode = strategy.hashCode(property(locator, "to", theTo), currentHashCode, theTo);
        }
        {
            java.lang.Integer theStartRowNum;
            theStartRowNum = this.getStartRowNum();
            currentHashCode = strategy.hashCode(property(locator, "startRowNum", theStartRowNum), currentHashCode, theStartRowNum);
        }
        {
            java.lang.Integer thePageSize;
            thePageSize = this.getPageSize();
            currentHashCode = strategy.hashCode(property(locator, "pageSize", thePageSize), currentHashCode, thePageSize);
        }
        {
            SortField theSortField;
            theSortField = this.getSortField();
            currentHashCode = strategy.hashCode(property(locator, "sortField", theSortField), currentHashCode, theSortField);
        }
        {
            Boolean theSearchAnyError;
            theSearchAnyError = this.isSearchAnyError();
            currentHashCode = strategy.hashCode(property(locator, "searchAnyError", theSearchAnyError), currentHashCode, theSearchAnyError);
        }
        {
            String theError;
            theError = this.getError();
            currentHashCode = strategy.hashCode(property(locator, "error", theError), currentHashCode, theError);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof AuditQuery)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final AuditQuery that = ((AuditQuery) object);
        {
            Long lhsInitiator;
            lhsInitiator = this.getInitiator();
            Long rhsInitiator;
            rhsInitiator = that.getInitiator();
            if (!strategy.equals(property(thisLocator, "initiator", lhsInitiator), property(thatLocator, "initiator", rhsInitiator), lhsInitiator, rhsInitiator)) {
                return false;
            }
        }
        {
            String lhsModule;
            lhsModule = this.getModule();
            String rhsModule;
            rhsModule = that.getModule();
            if (!strategy.equals(property(thisLocator, "module", lhsModule), property(thatLocator, "module", rhsModule), lhsModule, rhsModule)) {
                return false;
            }
        }
        {
            String lhsOperationKey;
            lhsOperationKey = this.getOperationKey();
            String rhsOperationKey;
            rhsOperationKey = that.getOperationKey();
            if (!strategy.equals(property(thisLocator, "operationKey", lhsOperationKey), property(thatLocator, "operationKey", rhsOperationKey), lhsOperationKey, rhsOperationKey)) {
                return false;
            }
        }
        {
            String lhsDescription;
            lhsDescription = this.getDescription();
            String rhsDescription;
            rhsDescription = that.getDescription();
            if (!strategy.equals(property(thisLocator, "description", lhsDescription), property(thatLocator, "description", rhsDescription), lhsDescription, rhsDescription)) {
                return false;
            }
        }
        {
            XMLGregorianCalendar lhsFrom;
            lhsFrom = this.getFrom();
            XMLGregorianCalendar rhsFrom;
            rhsFrom = that.getFrom();
            if (!strategy.equals(property(thisLocator, "from", lhsFrom), property(thatLocator, "from", rhsFrom), lhsFrom, rhsFrom)) {
                return false;
            }
        }
        {
            XMLGregorianCalendar lhsTo;
            lhsTo = this.getTo();
            XMLGregorianCalendar rhsTo;
            rhsTo = that.getTo();
            if (!strategy.equals(property(thisLocator, "to", lhsTo), property(thatLocator, "to", rhsTo), lhsTo, rhsTo)) {
                return false;
            }
        }
        {
            java.lang.Integer lhsStartRowNum;
            lhsStartRowNum = this.getStartRowNum();
            java.lang.Integer rhsStartRowNum;
            rhsStartRowNum = that.getStartRowNum();
            if (!strategy.equals(property(thisLocator, "startRowNum", lhsStartRowNum), property(thatLocator, "startRowNum", rhsStartRowNum), lhsStartRowNum, rhsStartRowNum)) {
                return false;
            }
        }
        {
            java.lang.Integer lhsPageSize;
            lhsPageSize = this.getPageSize();
            java.lang.Integer rhsPageSize;
            rhsPageSize = that.getPageSize();
            if (!strategy.equals(property(thisLocator, "pageSize", lhsPageSize), property(thatLocator, "pageSize", rhsPageSize), lhsPageSize, rhsPageSize)) {
                return false;
            }
        }
        {
            SortField lhsSortField;
            lhsSortField = this.getSortField();
            SortField rhsSortField;
            rhsSortField = that.getSortField();
            if (!strategy.equals(property(thisLocator, "sortField", lhsSortField), property(thatLocator, "sortField", rhsSortField), lhsSortField, rhsSortField)) {
                return false;
            }
        }
        {
            Boolean lhsSearchAnyError;
            lhsSearchAnyError = this.isSearchAnyError();
            Boolean rhsSearchAnyError;
            rhsSearchAnyError = that.isSearchAnyError();
            if (!strategy.equals(property(thisLocator, "searchAnyError", lhsSearchAnyError), property(thatLocator, "searchAnyError", rhsSearchAnyError), lhsSearchAnyError, rhsSearchAnyError)) {
                return false;
            }
        }
        {
            String lhsError;
            lhsError = this.getError();
            String rhsError;
            rhsError = that.getError();
            if (!strategy.equals(property(thisLocator, "error", lhsError), property(thatLocator, "error", rhsError), lhsError, rhsError)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    /**
     * @serialData serializing (initiator, module, operationKey, description, startRowNum, pageSize and sortField by default), from, to
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(from == null ? null : format(from));
        stream.writeObject(to == null ? null : format(to));
    }

    /**
     * @serialData deserializing (initiator, module, operationKey, description, startRowNum, pageSize and sortField by default), from, to
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        String from = (String) stream.readObject();
        this.from = from == null ? null : toXMLGregorianCalendar(format(from));
        String to = (String) stream.readObject();
        this.to = to == null ? null : toXMLGregorianCalendar(format(to));
    }
}
