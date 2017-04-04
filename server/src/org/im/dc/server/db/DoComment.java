package org.im.dc.server.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

public interface DoComment {
    @Insert("INSERT INTO Comments (articleId,created,author,comment) "
            + "VALUES(#{articleId},#{created},#{author},#{comment})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    void insertComment(RecComment comment);

    @Select("SELECT * FROM Comments WHERE articleId = #{articleId}")
    List<RecComment> retrieveComments(int articleId);

    @Select("SELECT * FROM Comments WHERE commentId = #{commentId}")
    RecComment getComment(int commentId);
}
