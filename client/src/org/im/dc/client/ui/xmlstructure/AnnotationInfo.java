package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.xs.XSAnnotation;

public class AnnotationInfo {
    private static final Pattern RE_DETAILS = Pattern.compile("(.+)\\{(.+)\\}");
    private static final Pattern RE_COLOR = Pattern.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    public final String text;
    public Color fgColor, bgColor, borderColor;
    public String type;

    public AnnotationInfo(XSAnnotation ann) {
        if (ann == null) {
            text = null;
            return;
        }
        String t = ann.getAnnotationString().replaceAll("<[^<]+>", "").trim();
        Matcher m = RE_DETAILS.matcher(t);
        if (m.matches()) {
            text = m.group(1);
            for (String cn : m.group(2).split(",")) {
                int p = cn.indexOf(':');
                if (p < 0) {
                    System.err.println("Wrong settings: " + cn);
                    continue;
                }
                String key = cn.substring(0, p);
                String value = cn.substring(p + 1);
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
                    type = value;
                    break;
                default:
                    System.err.println("Wrong color: " + cn);
                    break;
                }
            }
        } else {
            text = t;
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
}
