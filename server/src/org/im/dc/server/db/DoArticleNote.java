package org.im.dc.server.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface DoArticleNote {
    @Insert("INSERT INTO ArticleNotes (articleId,creator,note) VALUES(#{articleId},#{creator},#{note})")
    void insertNote(RecArticleNote note);

    @Insert("DELETE FROM ArticleNotes WHERE articleId = #{0} AND creator = #{1}")
    void deleteNote(int articleId, String user);

    @Select("SELECT * FROM ArticleNotes WHERE articleId = #{0} AND creator = #{1}")
    RecArticleNote getNote(int articleId, String user);
}
