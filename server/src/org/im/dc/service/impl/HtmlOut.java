package org.im.dc.service.impl;

import org.im.dc.server.js.JsDomWrapper;

public class HtmlOut {
    private final StringBuilder out = new StringBuilder(4096);

    public HtmlOut tag(String tag) {
        out.append(tag);
        return this;
    }

    public HtmlOut text(String text) {
        text = text.replace('+', '\u0301');
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        out.append(text);
        return this;
    }

    public HtmlOut text(JsDomWrapper w) {
        text((String) w.get("textContent"));
        return this;
    }

    public void log(String text) {
        System.err.println(text);
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
