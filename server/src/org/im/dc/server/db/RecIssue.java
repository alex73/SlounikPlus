package org.im.dc.server.db;

import java.util.Date;

public class RecIssue {
    private int issueId;
    // артыкул
    private int articleId;
    // калі быў створаны каментар
    private Date created;
    // хто стварыў каментар
    private String author;
    // тэкст заўвагі
    private String comment;
    // стары XML
    private byte[] oldXml;
    // прапанаваны XML
    private byte[] newXml;
    // калі выправілі
    private Date fixed;
    // хто выправіў
    private String fixer;
    // ці прынята
    private boolean accepted;

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public byte[] getOldXml() {
        return oldXml;
    }

    public void setOldXml(byte[] oldXml) {
        this.oldXml = oldXml;
    }

    public byte[] getNewXml() {
        return newXml;
    }

    public void setNewXml(byte[] newXml) {
        this.newXml = newXml;
    }

    public Date getFixed() {
        return fixed;
    }

    public void setFixed(Date fixed) {
        this.fixed = fixed;
    }

    public String getFixer() {
        return fixer;
    }

    public void setFixer(String fixer) {
        this.fixer = fixer;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
