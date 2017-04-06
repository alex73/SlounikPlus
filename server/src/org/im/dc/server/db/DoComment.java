package org.im.dc.server.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface DoComment {
    RecComment getComment(@Param("commentId") int commentId);

    List<RecComment> retrieveArticleComments(@Param("articleId") int articleId);

    List<RecComment> retrieveUserComments(@Param("user") String user);

    void insertComment(@Param("comment") RecComment comment);
}
