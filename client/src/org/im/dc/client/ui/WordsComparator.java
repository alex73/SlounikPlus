package org.im.dc.client.ui;

import java.util.Comparator;

public class WordsComparator implements Comparator<String> {
    public static final WordsComparator INSTANCE = new WordsComparator();

    @Override
    public int compare(String o1, String o2) {
        String s1 = charsOnly(o1);
        String s2 = charsOnly(o2);
        int r = s1.compareToIgnoreCase(s2);
        if (r == 0) {
            r = o1.toString().compareTo(o2.toString());
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
