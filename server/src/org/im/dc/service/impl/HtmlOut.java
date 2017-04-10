package org.im.dc.service.impl;

import java.util.List;

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

    /**
     * Толькі першы элемэнт, калі існуе.
     */
    public HtmlOut out(List<JsDomWrapper> list) {
        if (list == null) {
            return this;
        }
        switch (list.size()) {
        case 0:
            break;
        case 1:
            text((String) list.get(0).get("textContent"));
            break;
        default:
            throw new RuntimeException("Зашмат элементаў: " + list.size() + " 0: " + list.get(0).get("textContent")
                    + " 1: " + list.get(1).get("textContent"));
        }
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
