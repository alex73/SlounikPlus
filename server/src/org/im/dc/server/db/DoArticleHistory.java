package org.im.dc.server.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface DoArticleHistory {

    @Insert("INSERT INTO ArticlesHistory (articleId,changed,changer,oldState,newState,oldWords,newWords,oldAssignedUsers,newAssignedUsers,oldXml,newXml) "
            + "VALUES(#{articleId},#{changed},#{changer},#{oldState},#{newState},#{oldWords,typeHandler=StringArrayTypeHandler},#{newWords,typeHandler=StringArrayTypeHandler},#{oldAssignedUsers,typeHandler=StringArrayTypeHandler},#{newAssignedUsers,typeHandler=StringArrayTypeHandler},#{oldXml},#{newXml})")
    @Options(useGeneratedKeys = true, keyProperty = "historyId")
    void insertArticleHistory(RecArticleHistory hist);
}