
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
 *         &lt;element ref="{}zah"/>
 *         &lt;element ref="{}tlum" maxOccurs="unbounded"/>
 *         &lt;element name="ustterm" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ustfraz" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ustetym" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
    "zah",
    "tlum",
    "ustterm",
    "ustfraz",
    "ustetym"
})
@XmlRootElement(name = "root")
public class Root {

    @XmlElement(required = true)
    protected Zah zah;
    @XmlElement(required = true)
    protected List<Tlum> tlum;
    protected List<String> ustterm;
    protected List<String> ustfraz;
    protected List<String> ustetym;

    /**
     * Gets the value of the zah property.
     * 
     * @return
     *     possible object is
     *     {@link Zah }
     *     
     */
    public Zah getZah() {
        return zah;
    }

    /**
     * Sets the value of the zah property.
     * 
     * @param value
     *     allowed object is
     *     {@link Zah }
     *     
     */
    public void setZah(Zah value) {
        this.zah = value;
    }

    /**
     * Gets the value of the tlum property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tlum property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTlum().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Tlum }
     * 
     * 
     */
    public List<Tlum> getTlum() {
        if (tlum == null) {
            tlum = new ArrayList<Tlum>();
        }
        return this.tlum;
    }

    /**
     * Gets the value of the ustterm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ustterm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUstterm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUstterm() {
        if (ustterm == null) {
            ustterm = new ArrayList<String>();
        }
        return this.ustterm;
    }

    /**
     * Gets the value of the ustfraz property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ustfraz property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUstfraz().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUstfraz() {
        if (ustfraz == null) {
            ustfraz = new ArrayList<String>();
        }
        return this.ustfraz;
    }

    /**
     * Gets the value of the ustetym property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ustetym property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUstetym().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUstetym() {
        if (ustetym == null) {
            ustetym = new ArrayList<String>();
        }
        return this.ustetym;
    }

}
