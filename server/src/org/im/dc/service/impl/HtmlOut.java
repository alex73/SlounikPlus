package org.im.dc.service.impl;

import org.im.dc.server.js.JsDomWrapper;

public class HtmlOut {
    private final StringBuilder out = new StringBuilder(4096);

    public void tag(String tag) {
        out.append(tag);
    }

    public void text(String text) {
        out.append(text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
    }

    public void text(JsDomWrapper w) {
        text((String) w.get("textContent"));
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
