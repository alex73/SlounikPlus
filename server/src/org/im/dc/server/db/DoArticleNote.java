package org.im.dc.server.db;

import org.apache.ibatis.annotations.Param;

public interface DoArticleNote {

    RecArticleNote getNote(@Param("articleId") int articleId, @Param("user") String user);

    void insertNote(@Param("note") RecArticleNote note);

    void deleteNote(@Param("articleId") int articleId, @Param("user") String user);
}
