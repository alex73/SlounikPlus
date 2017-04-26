package org.im.dc.service.impl;

import org.im.dc.server.js.JsDomWrapper;

public class HtmlOut {
    private final StringBuilder out = new StringBuilder(4096);

    protected static String escape(String text) {
        text = text.replace('+', '\u0301');
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        return text;
    }

    public HtmlOut tag(String tag) {
        out.append(tag);
        return this;
    }

    public String prepare(String tagb, Object w, String tage) {
        String t = prepare(w);
        if (!t.isEmpty()) {
            return tagb + t + tage;
        } else {
            return "";
        }
    }

    public String prepare(Object w) {
        if (w instanceof JsDomWrapper) {
            return escape((String) ((JsDomWrapper) w).get("textContent"));
        } else {
            return escape(w.toString());
        }
    }

    public void log(String text) {
        System.err.println(text);
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
