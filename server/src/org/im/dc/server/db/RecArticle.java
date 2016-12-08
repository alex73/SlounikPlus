package org.im.dc.server.db;

/**
 * Record from Articles table representation.
 */
public class RecArticle {
    private int id;
    private String words;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }
}
