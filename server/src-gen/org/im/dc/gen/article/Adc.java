
package org.im.dc.gen.article;

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
 *         &lt;element name="sem" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="grpam" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="styl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="zaha" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{}ex" maxOccurs="unbounded" minOccurs="0"/>
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
    "sem",
    "grpam",
    "styl",
    "zaha",
    "desc",
    "ex"
})
@XmlRootElement(name = "adc")
public class Adc {

    protected String sem;
    protected String grpam;
    protected String styl;
    protected String zaha;
    @XmlElement(required = true)
    protected String desc;
    protected List<Ex> ex;

    /**
     * Gets the value of the sem property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSem() {
        return sem;
    }

    /**
     * Sets the value of the sem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSem(String value) {
        this.sem = value;
    }

    /**
     * Gets the value of the grpam property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrpam() {
        return grpam;
    }

    /**
     * Sets the value of the grpam property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrpam(String value) {
        this.grpam = value;
    }

    /**
     * Gets the value of the styl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyl() {
        return styl;
    }

    /**
     * Sets the value of the styl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyl(String value) {
        this.styl = value;
    }

    /**
     * Gets the value of the zaha property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZaha() {
        return zaha;
    }

    /**
     * Sets the value of the zaha property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZaha(String value) {
        this.zaha = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Gets the value of the ex property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ex property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEx().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Ex }
     * 
     * 
     */
    public List<Ex> getEx() {
        if (ex == null) {
            ex = new ArrayList<Ex>();
        }
        return this.ex;
    }

}
