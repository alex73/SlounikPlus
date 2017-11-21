package org.im.dc.server.db;

import java.util.Date;

import org.im.dc.service.dto.Related;

public class RecComment {
    private int commentId;
    // артыкул
    private int articleId;
    // калі быў створаны каментар
    private Date created;
    // хто стварыў каментар
    private String author;
    // тэкст каментару
    private String comment;

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
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

    public Related getRelated() {
        Related r = new Related();
        r.type = Related.RelatedType.COMMENT;
        r.articleId = articleId;
        r.id = commentId;
        r.articleId = articleId;
        r.when = created;
        r.who = author;
        r.what = comment;
        r.sk = "К";
        return r;
    }
}
