package org.im.dc.client.ui;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class WordsComparator implements Comparator<String> {
    public static WordsComparator INSTANCE;

    public final Locale loc;
    public final Collator collator;

    public static void init(String locale) {
        INSTANCE = new WordsComparator(locale);
    }

    public WordsComparator(String locale) {
        loc = new Locale(locale);
        collator = Collator.getInstance(loc);
    }

    @Override
    public int compare(String o1, String o2) {
        String s1 = charsOnly(o1);
        String s2 = charsOnly(o2);
        int r;
        if (s1.isEmpty() && s2.isEmpty()) {
            r = 0;
        } else if (s1.isEmpty()) {
            r = 1;
        } else if (s2.isEmpty()) {
            r = -1;
        } else {
            r = collator.compare(s1.toLowerCase(loc), s2.toLowerCase(loc));
            if (r == 0) {
                r = o1.toString().compareTo(o2.toString());
            }
        }
        return r;
    }

    String charsOnly(String v) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < v.length(); i++) {
            if (Character.isLetterOrDigit(v.charAt(i))) {
                r.append(v.charAt(i));
            }
        }
        return r.toString();
    }
}
