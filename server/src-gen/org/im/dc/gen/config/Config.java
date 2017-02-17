
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
 *         &lt;element ref="{}users"/>
 *         &lt;element ref="{}roles"/>
 *         &lt;element ref="{}states"/>
 *         &lt;element ref="{}external_links"/>
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
    "users",
    "roles",
    "states",
    "externalLinks"
})
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    protected Users users;
    @XmlElement(required = true)
    protected Roles roles;
    @XmlElement(required = true)
    protected States states;
    @XmlElement(name = "external_links", required = true)
    protected ExternalLinks externalLinks;

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
     * Gets the value of the externalLinks property.
     * 
     * @return
     *     possible object is
     *     {@link ExternalLinks }
     *     
     */
    public ExternalLinks getExternalLinks() {
        return externalLinks;
    }

    /**
     * Sets the value of the externalLinks property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExternalLinks }
     *     
     */
    public void setExternalLinks(ExternalLinks value) {
        this.externalLinks = value;
    }

}
