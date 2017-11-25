package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.xs.XSAnnotation;

public class AnnotationInfo {
    private static final Pattern RE_DETAILS = Pattern.compile("\\{.+?\\}");
    private static final Pattern RE_COLOR = Pattern.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    enum EDIT_TYPE {
        DEFAULT, COMBO_EDITABLE, COMBO_DICT_EDITABLE, RADIO, CHECK, CUSTOM, ARTICLES_LIST
    };

    public String text;
    public Color fgColor, bgColor, borderColor;
    public EDIT_TYPE editType = EDIT_TYPE.DEFAULT;
    public String editDetails;
    public List<RORW> enables = new ArrayList<>();

    public AnnotationInfo(XSAnnotation ann, String tagName) {
        if (ann != null) {
            String t = ann.getAnnotationString().replaceAll("<[^<]+>", "").trim();
            Matcher m = RE_DETAILS.matcher(t);
            while (m.find()) {
                processSetting(m.group(0));
            }
            text = m.replaceAll("");
        }
        if (text == null || text.trim().isEmpty()) {
            text = tagName.replace('_', ' ');
        }
    }

    private void processSetting(String s) {
        String cn = s.substring(1, s.length() - 1);
        int p = cn.indexOf(':');
        String key, value;
        if (p < 0) {
            key = cn;
            value = null;
        } else {
            key = cn.substring(0, p);
            value = cn.substring(p + 1);
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
            p = value.indexOf('/');
            if (p <= 0) {
                throw new RuntimeException("Wrong annotation: " + cn);
            }
            r.role = value.substring(0, p);
            r.state = value.substring(p + 1);
            break;
        default:
            throw new RuntimeException("Wrong annotation: " + cn);
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

    public static class RORW {
        boolean writable;
        public String role;
        public String state;
    }
}
