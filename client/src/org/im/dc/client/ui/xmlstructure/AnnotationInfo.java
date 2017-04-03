package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.xs.XSAnnotation;

public class AnnotationInfo {
    private static final Pattern RE_COLOR = Pattern.compile("(.+)\\{(.+)\\}");
    private static final Pattern RE_COLOR_ONE = Pattern
            .compile("([a-z]+):#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    public final String text;
    public Color fgColor, bgColor, borderColor;

    public AnnotationInfo(XSAnnotation ann) {
        if (ann == null) {
            text = null;
            return;
        }
        String t = ann.getAnnotationString().replaceAll("<[^<]+>", "").trim();
        Matcher m = RE_COLOR.matcher(t);
        if (m.matches()) {
            text = m.group(1);
            for (String cn : m.group(2).split(",")) {
                m = RE_COLOR_ONE.matcher(cn);
                if (!m.matches()) {
                    System.err.println("Wrong color: " + cn);
                    continue;
                }
                Color c = new Color(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16),
                        Integer.parseInt(m.group(4), 16));
                switch (m.group(1)) {
                case "fg":
                    fgColor = c;
                    break;
                case "bg":
                    bgColor = c;
                    break;
                case "border":
                    borderColor = c;
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
}
