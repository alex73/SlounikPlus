package org.im.dc.service.dto;

public class ArticlesFilter {
    public String state;
    public String user;
    public String word;
    public String text;

    public String getLikeText() {
        if (text == null) {
            return null;
        }
        return "% " + text.replace('*', '%') + " %";
    }

    public String getLikeWord() {
        if (word == null) {
            return null;
        }
        return word.replace('*', '%');
    }
}
