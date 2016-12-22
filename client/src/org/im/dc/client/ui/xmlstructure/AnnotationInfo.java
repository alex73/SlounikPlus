package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.xs.XSAnnotation;

public class AnnotationInfo {
    private static final Pattern RE_COLOR = Pattern.compile("(.+)/#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    public final String text;
    public Color color;

    public AnnotationInfo(XSAnnotation ann) {
        if (ann == null) {
            text = null;
            return;
        }
        String t = ann.getAnnotationString().replaceAll("<[^<]+>", "").trim();
        Matcher m = RE_COLOR.matcher(t);
        if (m.matches()) {
            text = m.group(1);
            color = new Color(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16),
                    Integer.parseInt(m.group(4), 16));
        } else {
            text = t;
        }
    }
}
