
package org.im.dc.gen.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="headerLocale" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="stress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{}roles"/>
 *         &lt;element ref="{}users"/>
 *         &lt;element ref="{}states"/>
 *         &lt;element ref="{}types"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "version",
    "headerLocale",
    "stress",
    "roles",
    "users",
    "states",
    "types"
})
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected String headerLocale;
    @XmlElement(required = true)
    protected String stress;
    @XmlElement(required = true)
    protected Roles roles;
    @XmlElement(required = true)
    protected Users users;
    @XmlElement(required = true)
    protected States states;
    @XmlElement(required = true)
    protected Types types;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the headerLocale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaderLocale() {
        return headerLocale;
    }

    /**
     * Sets the value of the headerLocale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaderLocale(String value) {
        this.headerLocale = value;
    }

    /**
     * Gets the value of the stress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStress() {
        return stress;
    }

    /**
     * Sets the value of the stress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStress(String value) {
        this.stress = value;
    }

    /**
     * Gets the value of the roles property.
     * 
     * @return
     *     possible object is
     *     {@link Roles }
     *     
     */
    public Roles getRoles() {
        return roles;
    }

    /**
     * Sets the value of the roles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Roles }
     *     
     */
    public void setRoles(Roles value) {
        this.roles = value;
    }

    /**
     * Gets the value of the users property.
     * 
     * @return
     *     possible object is
     *     {@link Users }
     *     
     */
    public Users getUsers() {
        return users;
    }

    /**
     * Sets the value of the users property.
     * 
     * @param value
     *     allowed object is
     *     {@link Users }
     *     
     */
    public void setUsers(Users value) {
        this.users = value;
    }

    /**
     * Gets the value of the states property.
     * 
     * @return
     *     possible object is
     *     {@link States }
     *     
     */
    public States getStates() {
        return states;
    }

    /**
     * Sets the value of the states property.
     * 
     * @param value
     *     allowed object is
     *     {@link States }
     *     
     */
    public void setStates(States value) {
        this.states = value;
    }

    /**
     * Gets the value of the types property.
     * 
     * @return
     *     possible object is
     *     {@link Types }
     *     
     */
    public Types getTypes() {
        return types;
    }

    /**
     * Sets the value of the types property.
     * 
     * @param value
     *     allowed object is
     *     {@link Types }
     *     
     */
    public void setTypes(Types value) {
        this.types = value;
    }

}
