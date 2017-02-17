
package org.im.dc.gen.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for permission.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="permission">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="edit_header"/>
 *     &lt;enumeration value="statistics"/>
 *     &lt;enumeration value="view_output"/>
 *     &lt;enumeration value="full_validation"/>
 *     &lt;enumeration value="add_words"/>
 *     &lt;enumeration value="reassign"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "permission")
@XmlEnum
public enum Permission {

    @XmlEnumValue("edit_header")
    EDIT_HEADER("edit_header"),
    @XmlEnumValue("statistics")
    STATISTICS("statistics"),
    @XmlEnumValue("view_output")
    VIEW_OUTPUT("view_output"),
    @XmlEnumValue("full_validation")
    FULL_VALIDATION("full_validation"),
    @XmlEnumValue("add_words")
    ADD_WORDS("add_words"),
    @XmlEnumValue("reassign")
    REASSIGN("reassign");
    private final String value;

    Permission(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Permission fromValue(String v) {
        for (Permission c: Permission.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
