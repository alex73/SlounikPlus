
package org.im.dc.gen.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for typePermission.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="typePermission">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="view_output"/>
 *     &lt;enumeration value="add_articles"/>
 *     &lt;enumeration value="add_article"/>
 *     &lt;enumeration value="propose_changes"/>
 *     &lt;enumeration value="reassign"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "typePermission")
@XmlEnum
public enum TypePermission {


    /**
     * Ці можа карыстальнік глядзець як будзе выглядае артыкул на паперы
     * 
     */
    @XmlEnumValue("view_output")
    VIEW_OUTPUT("view_output"),

    /**
     * Ці можа карыстальнік дадаваць новыя артыкулы праз адмысловую форму
     * 
     */
    @XmlEnumValue("add_articles")
    ADD_ARTICLES("add_articles"),

    /**
     * Ці можа карыстальнік дадаваць адзін артыкул праз рэдагаванне
     * 
     */
    @XmlEnumValue("add_article")
    ADD_ARTICLE("add_article"),

    /**
     * Ці можа карыстальнік прапаноўваць змены ў артыкуле
     * 
     */
    @XmlEnumValue("propose_changes")
    PROPOSE_CHANGES("propose_changes"),

    /**
     * Ці можа карыстальнік пераназначаць словы
     * 
     */
    @XmlEnumValue("reassign")
    REASSIGN("reassign");
    private final String value;

    TypePermission(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypePermission fromValue(String v) {
        for (TypePermission c: TypePermission.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
