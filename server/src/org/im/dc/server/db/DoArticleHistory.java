package org.im.dc.server.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

public interface DoArticleHistory {

    @Insert("INSERT INTO ArticlesHistory (articleId,changed,changer,oldState,newState,oldWords,newWords,oldAssignedUsers,newAssignedUsers,oldXml,newXml) "
            + "VALUES(#{articleId},#{changed},#{changer},#{oldState},#{newState},#{oldWords,typeHandler=StringArrayTypeHandler},#{newWords,typeHandler=StringArrayTypeHandler},#{oldAssignedUsers,typeHandler=StringArrayTypeHandler},#{newAssignedUsers,typeHandler=StringArrayTypeHandler},#{oldXml},#{newXml})")
    @Options(useGeneratedKeys = true, keyProperty = "historyId")
    void insertArticleHistory(RecArticleHistory hist);

    @Select("SELECT * FROM ArticlesHistory WHERE articleId = #{articleId} ORDER BY changed DESC")
    List<RecArticleHistory> retrieveHistory(int articleId);
}
