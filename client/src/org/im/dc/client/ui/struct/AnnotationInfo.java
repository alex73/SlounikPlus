package org.im.dc.client.ui.struct;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.apache.xerces.xs.XSAnnotation;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnotationInfo {
    private static final Pattern RE_COLOR = Pattern.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    public enum EDIT_TYPE {
        TEXT, COMBO, COMBO_FILTERED, RADIO, CHECK, ARTICLES_LIST
    };

    public String text, tooltip;
    public Color fgColor, bgColor, borderColor;
    public EDIT_TYPE editType;
    public String editDetails;
    public Class<?> customImpl;
    public List<RORW> enables = new ArrayList<>();
    public int overrideMinOccurs = -1;
    public int overrideMaxOccurs = -1;

    public AnnotationInfo(XSAnnotation ann) {
        if (ann != null) {
            ann.writeAnnotation(parser, XSAnnotation.SAX_CONTENTHANDLER);
        }
    }

    private void processSetting(String key, String value) {
        if (key == null) {
            throw new RuntimeException("Wrong annotation without key: " + value);
        }
        switch (key) {
        case "fg":
            fgColor = parseColor(value);
            break;
        case "bg":
            bgColor = parseColor(value);
            break;
        case "border":
            borderColor = parseColor(value);
            break;
        case "type":
            int pp = value.indexOf('/');
            editType = EDIT_TYPE.valueOf(value.substring(0, pp).toUpperCase());
            editDetails = value.substring(pp + 1);
            break;
        case "ro":
        case "rw":
            RORW r = new RORW();
            enables.add(r);
            r.writable = "rw".equals(key);
            int p = value.indexOf('/');
            if (p <= 0) {
                throw new RuntimeException("Wrong annotation: " + value);
            }
            r.role = value.substring(0, p);
            r.state = value.substring(p + 1);
            break;
        case "overrideMinOccurs":
            overrideMinOccurs = Integer.parseInt(value);
            break;
        case "overrideMaxOccurs":
            overrideMaxOccurs = Integer.parseInt(value);
            break;
        case "custom":
            try {
                customImpl = Class.forName(value);
            } catch (Exception ex) {
                throw new RuntimeException("Error find class for custom UI: " + value, ex);
            }
            break;
        default:
            throw new RuntimeException("Wrong annotation: " + key + ":" + value);
        }
    }

    public void applyColors(JComponent comp, IXSContainer parentContainer) {
        if (bgColor != null) {
            comp.setBackground(bgColor);
            comp.setOpaque(true);
        } else {
            comp.setOpaque(false);
        }
        if (fgColor != null) {
            comp.setForeground(fgColor);
        } else if (parentContainer != null) {
            comp.setForeground(parentContainer.getUIComponent().getForeground());
        }
    }

    private Color parseColor(String v) {
        Matcher m = RE_COLOR.matcher(v.trim());
        if (!m.matches()) {
            System.err.println("Wrong color: " + v);
            return null;
        }
        return new Color(Integer.parseInt(m.group(1), 16), Integer.parseInt(m.group(2), 16),
                Integer.parseInt(m.group(3), 16));
    }

    ContentHandler parser = new DefaultHandler() {
        String attrName;
        StringBuilder str = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            attrName = null;
            str.setLength(0);
            switch (localName) {
            case "appinfo":
                for (int i = 0; i < attributes.getLength(); i++) {
                    switch (attributes.getLocalName(i)) {
                    case "attr":
                        attrName = attributes.getValue(i);
                        break;
                    }
                }
                break;
            case "documentation":
                for (int i = 0; i < attributes.getLength(); i++) {
                    switch (attributes.getLocalName(i)) {
                    case "type":
                        attrName = attributes.getValue(i);
                        break;
                    }
                }
                break;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            str.append(ch, start, length);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName) {
            case "appinfo":
                processSetting(attrName, str.toString());
                break;
            case "documentation":
                if ("tooltip".equals(attrName)) {
                    tooltip = str.toString();
                } else if (attrName == null) {
                    text = str.toString();
                }
                break;
            }
            str.setLength(0);
        }
    };

    public static class RORW {
        boolean writable;
        public String role;
        public String state;
    }
}
