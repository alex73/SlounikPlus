
package org.im.dc.gen.config;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{}version"/>
 *         &lt;element ref="{}headerLocale"/>
 *         &lt;element ref="{}stress"/>
 *         &lt;element ref="{}users"/>
 *         &lt;element ref="{}types"/>
 *         &lt;element ref="{}roles"/>
 *         &lt;element ref="{}permissions" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}states" maxOccurs="unbounded" minOccurs="0"/>
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
    "users",
    "types",
    "roles",
    "permissions",
    "states"
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
    protected Users users;
    @XmlElement(required = true)
    protected Types types;
    @XmlElement(required = true)
    protected Roles roles;
    protected List<Permissions> permissions;
    protected List<States> states;

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
     * Gets the value of the permissions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permissions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermissions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Permissions }
     * 
     * 
     */
    public List<Permissions> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<Permissions>();
        }
        return this.permissions;
    }

    /**
     * Gets the value of the states property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the states property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStates().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link States }
     * 
     * 
     */
    public List<States> getStates() {
        if (states == null) {
            states = new ArrayList<States>();
        }
        return this.states;
    }

}
