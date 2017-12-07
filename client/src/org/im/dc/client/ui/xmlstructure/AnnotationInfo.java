package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.xs.XSAnnotation;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnotationInfo {
    private static final Pattern RE_COLOR = Pattern.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    enum EDIT_TYPE {
        DEFAULT, COMBO, COMBO_EDITABLE, RADIO, CHECK, CUSTOM, ARTICLES_LIST
    };

    public String text, tooltip;
    public Color fgColor, bgColor, borderColor;
    public EDIT_TYPE editType = EDIT_TYPE.DEFAULT;
    public String editDetails;
    public List<RORW> enables = new ArrayList<>();

    public AnnotationInfo(XSAnnotation ann, String tagName) {
        if (ann != null) {
            ann.writeAnnotation(parser, XSAnnotation.SAX_CONTENTHANDLER);
        }
        if (text == null || text.trim().isEmpty()) {
            text = tagName.replace('_', ' ');
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
        default:
            throw new RuntimeException("Wrong annotation: " + key + ":" + value);
        }
    }

    Color parseColor(String v) {
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
