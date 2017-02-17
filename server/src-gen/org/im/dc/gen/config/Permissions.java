
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
 *         &lt;element name="edit_header" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="statistics" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="view_output" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="full_validation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="add_words" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reassign" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "editHeader",
    "statistics",
    "viewOutput",
    "fullValidation",
    "addWords",
    "reassign"
})
@XmlRootElement(name = "permissions")
public class Permissions {

    @XmlElement(name = "edit_header", required = true)
    protected String editHeader;
    @XmlElement(required = true)
    protected String statistics;
    @XmlElement(name = "view_output", required = true)
    protected String viewOutput;
    @XmlElement(name = "full_validation", required = true)
    protected String fullValidation;
    @XmlElement(name = "add_words", required = true)
    protected String addWords;
    @XmlElement(required = true)
    protected String reassign;

    /**
     * Gets the value of the editHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEditHeader() {
        return editHeader;
    }

    /**
     * Sets the value of the editHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEditHeader(String value) {
        this.editHeader = value;
    }

    /**
     * Gets the value of the statistics property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatistics() {
        return statistics;
    }

    /**
     * Sets the value of the statistics property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatistics(String value) {
        this.statistics = value;
    }

    /**
     * Gets the value of the viewOutput property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getViewOutput() {
        return viewOutput;
    }

    /**
     * Sets the value of the viewOutput property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setViewOutput(String value) {
        this.viewOutput = value;
    }

    /**
     * Gets the value of the fullValidation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullValidation() {
        return fullValidation;
    }

    /**
     * Sets the value of the fullValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullValidation(String value) {
        this.fullValidation = value;
    }

    /**
     * Gets the value of the addWords property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddWords() {
        return addWords;
    }

    /**
     * Sets the value of the addWords property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddWords(String value) {
        this.addWords = value;
    }

    /**
     * Gets the value of the reassign property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReassign() {
        return reassign;
    }

    /**
     * Sets the value of the reassign property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReassign(String value) {
        this.reassign = value;
    }

}
