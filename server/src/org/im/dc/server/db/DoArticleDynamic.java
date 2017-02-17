package org.im.dc.server.db;

public class DoArticleDynamic {

    public static String list(String state) {
        String s = "SELECT articleId, words, state FROM Articles";
        if (state != null) {
            s += " WHERE state=#{state}";
        }
        return s;
    }
}
