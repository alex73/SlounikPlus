package org.im.dc.server.db;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

public interface DoIssue {
    @Insert("INSERT INTO Issues (articleId,created,author,comment,oldXml,newXml) "
            + "VALUES(#{articleId},#{created},#{author},#{comment},#{oldXml},#{newXml})")
    @Options(useGeneratedKeys = true, keyProperty = "issueId")
    void insertIssue(RecIssue issue);

    @Select("SELECT * FROM Issues WHERE articleId = #{articleId}")
    List<RecIssue> retrieveIssues(int articleId);
}
