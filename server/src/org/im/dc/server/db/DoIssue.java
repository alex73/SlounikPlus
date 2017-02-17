package org.im.dc.server.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface DoIssue {
    @Insert("INSERT INTO Issues (articleId,created,author,comment,oldXml,newXml) "
            + "VALUES(#{articleId},#{created},#{author},#{comment},#{oldXml},#{newXml})")
    @Options(useGeneratedKeys = true, keyProperty = "issueId")
    void insertIssue(RecIssue issue);
}
