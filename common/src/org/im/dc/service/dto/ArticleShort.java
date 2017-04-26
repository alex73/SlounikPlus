package org.im.dc.service.dto;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ArticleShort {
    static final Collator BE = Collator.getInstance(new Locale("be"));

    public int id;
    public String[] words;
    public String state;
    public String[] assignedUsers;
    public String validationError;

    public static void sortByAlphabet(List<ArticleShort> list) {
        Collections.sort(list, new Comparator<ArticleShort>() {
            @Override
            public int compare(ArticleShort a1, ArticleShort a2) {
                for (int index = 0;; index++) {
                    if (a1.words.length < index) {
                        return -1;
                    } else if (a2.words.length < index) {
                        return 1;
                    }
                    int r = BE.compare(a1.words[index].replace("+", ""), a2.words[index].replace("+", ""));
                    if (r != 0) {
                        return r;
                    }
                }
            }
        });
    }
}
