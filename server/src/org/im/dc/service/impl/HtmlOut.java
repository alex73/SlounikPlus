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

    // get latest non-space char
    public char latestNonSpace() {
        boolean tag = false;
        for (int i = out.length() - 1; i >= 0; i--) {
            char c = out.charAt(i);
            if (!tag && c == '>') {
                tag = true;
                continue;
            }
            if (tag && c == '<') {
                tag = false;
                continue;
            }
            if (!tag && !Character.isWhitespace(c)) {
                return c;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
