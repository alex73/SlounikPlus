package org.im.dc.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

    @Deprecated
    public HtmlOut tag(String tag) {
        out.append(tag);
        return this;
    }

    @Deprecated
    public String prepare(String tagb, Object w, String tage) {
        String t = prepare(w);
        if (!t.isEmpty()) {
            return tagb + t + tage;
        } else {
            return "";
        }
    }

    public String prepare(Object w) {
        if (w == null) {
            return "";
        }
        if (w instanceof List) {
            List<?> list = (List<?>) w;
            switch (list.size()) {
            case 0:
                return "";
            case 1:
                return escape((String) ((JsDomWrapper) list.get(0)).get("textContent"));
            default:
                throw new RuntimeException("Too many elements in the list");
            }
        } else if (w instanceof JsDomWrapper) {
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

    public void out(String tagb, Object w, String tage) {
        start(tagb, tage).add(w).end();
    }

    public OutPart start(String tagb, String tage) {
        OutPart p = new OutPart();
        p.tagb = tagb;
        p.tage = tage;
        p.separator = "";
        return p;
    }

    public OutPart start(String tagb, String tage, String separator) {
        OutPart p = new OutPart();
        p.tagb = tagb;
        p.tage = tage;
        p.separator = separator;
        return p;
    }

    @Override
    public String toString() {
        return out.toString();
    }

    public class OutPart {
        String tagb;
        String tage;
        String separator;
        private final StringBuilder pout = new StringBuilder(4096);

        public OutPart add(Object w) {
            String prepared = prepare(w);
            if (StringUtils.isEmpty(prepared)) {
                return OutPart.this;
            }
            if (pout.length() > 0) {
                pout.append(separator);
            }
            pout.append(prepared);
            return OutPart.this;
        }

        public void end() {
            if (pout.length() > 0) {
                out.append(tagb).append(pout).append(tage);
            }
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }
}
