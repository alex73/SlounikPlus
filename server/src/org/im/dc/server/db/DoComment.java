package org.im.dc.server.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface DoComment {
    @Insert("INSERT INTO Comments (articleId,created,author,comment) "
            + "VALUES(#{articleId},#{created},#{author},#{comment})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    void insertComment(RecComment comment);
}
