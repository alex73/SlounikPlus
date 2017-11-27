
package org.im.dc.gen.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for commonPermission.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="commonPermission">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="full_statistics"/>
 *     &lt;enumeration value="full_validation"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "commonPermission")
@XmlEnum
public enum CommonPermission {


    /**
     * Ці можа карыстальнік глядзець статыстыку па ўсіх
     * 
     */
    @XmlEnumValue("full_statistics")
    FULL_STATISTICS("full_statistics"),

    /**
     * Ці можа карыстальнік правяраць ўвесь слоўнік
     * 
     */
    @XmlEnumValue("full_validation")
    FULL_VALIDATION("full_validation");
    private final String value;

    CommonPermission(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CommonPermission fromValue(String v) {
        for (CommonPermission c: CommonPermission.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}