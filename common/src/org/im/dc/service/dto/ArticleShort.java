package org.im.dc.service.dto;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ArticleShort {
    static final Collator BE = Collator.getInstance(new Locale("be"));
    static final String[] EMPTY_LIST = new String[0];

    public int id;
    public String type;
    public String header;
    public String state;
    public String[] assignedUsers = EMPTY_LIST;
    public String validationError;

    public static void sortById(List<ArticleShort> list) {
        Collections.sort(list, new Comparator<ArticleShort>() {
            @Override
            public int compare(ArticleShort a1, ArticleShort a2) {
                return Integer.compare(a1.id, a2.id);
            }
        });
    }
}
