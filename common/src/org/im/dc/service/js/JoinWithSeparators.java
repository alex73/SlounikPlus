package org.im.dc.service.js;

import java.util.ArrayList;
import java.util.List;

public class JoinWithSeparators implements IHtmlPart {
    private final String sep, lastSep;
    protected List<IHtmlPart> children = new ArrayList<>();

    public JoinWithSeparators(String sep, String lastSep) {
        this.sep = sep;
        this.lastSep = lastSep;
    }

    public JoinWithSeparators text(String text) {
        children.add(new Text(text, true));
        return this;
    }

    public JoinWithSeparators requiredTag(String tag) {
        children.add(new Tag(tag, true));
        return this;
    }

    public JoinWithSeparators tag(String tag) {
        children.add(new Tag(tag, false));
        return this;
    }

    public JoinWithSeparators add(IHtmlPart part) {
        children.add(part);
        return this;
    }

    public JoinWithSeparators join() {
        JoinWithSeparators r = new JoinWithSeparators("", "");
        children.add(r);
        return r;
    }

    public JoinWithSeparators join(String tagSep) {
        JoinWithSeparators r = new JoinWithSeparators(tagSep, tagSep);
        children.add(r);
        return r;
    }

    public JoinWithSeparators join(String tagSep, String tagLastSep) {
        JoinWithSeparators r = new JoinWithSeparators(tagSep, tagLastSep);
        children.add(r);
        return r;
    }

    public void clear() {
        children.clear();
    }

    /**
     * It removes child levels if there is no required texts.
     */
    public void normalize() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof JoinWithSeparators) {
                ((JoinWithSeparators) children.get(i)).normalize();
                if (children.get(i).isEmpty()) {
                    children.remove(i);
                    i--;
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (IHtmlPart p : children) {
            if (!p.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean notEmpty() {
        return !isEmpty();
    }

    @Override
    public void out(StringBuilder out) {
        for (int i = 0; i < children.size(); i++) {
            if (i == 0) {
            } else if (i == children.size() - 1) {
                if (lastSep.length() > 0) {
                    out.append(lastSep);
                }
            } else {
                if (sep.length() > 0) {
                    out.append(sep);
                }
            }
            children.get(i).out(out);
        }
    }

    public String out() {
        StringBuilder o = new StringBuilder();
        out(o);
        return o.toString();
    }

    public void dump(String prefix) {
        System.err.println(prefix+"== join");
        for(IHtmlPart ch:children) {
            ch.dump(prefix+"  ");
        }
    }

    static class Text implements IHtmlPart {
        private final String text;
        private final boolean required;

        public Text(String text, boolean required) {
            if (text == null) {
                throw new NullPointerException();
            }
            this.text = text;
            this.required = required;
        }

        @Override
        public void out(StringBuilder out) {
            String t = text;
            t = t.replace('+', '\u0301');
            t = t.replace("&", "&amp;");
            t = t.replace("<", "&lt;");
            t = t.replace(">", "&gt;");
            out.append(t);
        }

        @Override
        public boolean isEmpty() {
            return !required;
        }

        @Override
        public boolean notEmpty() {
            return !isEmpty();
        }

        @Override
        public void dump(String prefix) {
            System.err.println(prefix + "== text: " + text);
        }
    }

    static class Tag implements IHtmlPart {
        private final String tag;
        private final boolean required;

        public Tag(String tag, boolean required) {
            this.tag = tag;
            this.required = required;
        }

        @Override
        public void out(StringBuilder out) {
            out.append(tag);
        }

        @Override
        public boolean isEmpty() {
            return !required;
        }

        @Override
        public boolean notEmpty() {
            return !isEmpty();
        }
        @Override
        public void dump(String prefix) {
            System.err.println(prefix + "== tag: " + tag);
        }
    }
}
