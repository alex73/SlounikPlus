package org.im.dc.server.db;

public class RecArticleNote {
    // артыкул
    private int articleId;
    // карыстальнік
    private String creator;
    // нататка
    private byte[] note;

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public byte[] getNote() {
        return note;
    }

    public void setNote(byte[] note) {
        this.note = note;
    }
}
