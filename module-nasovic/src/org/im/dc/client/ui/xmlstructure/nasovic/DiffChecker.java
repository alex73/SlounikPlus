package org.im.dc.client.ui.xmlstructure.nasovic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.diff.CommandVisitor;
import org.apache.commons.text.diff.EditScript;
import org.apache.commons.text.diff.StringsComparator;

public class DiffChecker {
    public static String diffText(String orig, String changed) {
        if (orig.equals(changed)) {
            return null;
        }

        String c1 = orig.trim().replaceAll("\\[.+\\]", "").toLowerCase();
        String c2 = changed.trim().toLowerCase();
        if (c1.equals(c2)) {
            return null;
        }

        List<Change> changes = new ArrayList<>();
        EditScript<Character> script = new StringsComparator(c1, c2).getScript();
        script.visit(new CommandVisitor<Character>() {
            @Override
            public void visitKeepCommand(Character c) {
                changes.add(new Change('=', c));
            }

            @Override
            public void visitInsertCommand(Character c) {
                changes.add(new Change('+', c));
            }

            @Override
            public void visitDeleteCommand(Character c) {
                changes.add(new Change('-', c));
            }
        });
        System.out.println();

        for (Change c : changes) {
            if (c.op == '=') {
                continue;
            }
            if (" ´,.{}()/\n".indexOf(c.str) >= 0) {
                if (c.op == '+') {
                    c.str = "";
                }
                c.op = '=';
            }
        }
        for (int i = 0; i < changes.size(); i++) {
            Change d = changes.get(i);
            if (d.str.isEmpty()) {
                changes.remove(i);
                i--;
            }
        }
        for (int i = 1; i < changes.size(); i++) {
            Change p = changes.get(i - 1);
            Change d = changes.get(i);
            if (p.op == d.op) {
                p.str += d.str;
                changes.remove(i);
                i--;
            }
        }
        if (changes.size() == 1) {
            return null;
        }
        for (Change c : changes) {
            if (c.op == '=' && c.str.length() > 15) {
                c.str = c.str.substring(0, 7) + "..." + c.str.substring(c.str.length() - 7);
            }
        }
        StringBuilder o = new StringBuilder();
        for (Change c : changes) {
            switch (c.op) {
            case '+':
                o.append("[+").append(c.str).append(']');
                break;
            case '-':
                o.append("[-").append(c.str).append(']');
                break;
            case '=':
                o.append(c.str);
                break;
            }
        }
        if (o.length() > 200) {
            o.setLength(200);
            o.append("...");
        }
        return o.toString();
    }

    static class Change {
        char op;
        String str;

        public Change(char op, char c) {
            this.op = op;
            this.str = "" + c;
        }

        @Override
        public String toString() {
            if (op == '=') {
                return str;
            } else {
                return op + str;
            }
        }
    }
}
