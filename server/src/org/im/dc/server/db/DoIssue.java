package org.im.dc.server.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface DoIssue {
    @Insert("INSERT INTO Issues (articleId,created,author,comment,oldXml,newXml) "
            + "VALUES(#{articleId},#{created},#{author},#{comment},#{oldXml},#{newXml})")
    @Options(useGeneratedKeys = true, keyProperty = "issueId")
    void insertIssue(RecIssue issue);

    @Update("UPDATE Issues SET accepted=#{1}, fixer=#{2}, fixed=#{3} WHERE issueId = #{0}")
    void fixIssue(int issueId, boolean accepted, String fixer, Date fixed);

    @Select("SELECT * FROM Issues WHERE articleId = #{articleId}")
    List<RecIssue> retrieveIssues(int articleId);

    List<RecIssue> retrieveUserIssues(@Param("user") String user);

    @Select("SELECT * FROM Issues WHERE issueId = #{issueId}")
    RecIssue getIssue(int issueId);
}
