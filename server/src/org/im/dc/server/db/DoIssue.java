package org.im.dc.server.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface DoIssue {
    void insertIssue(@Param("issue") RecIssue issue);

    void fixIssue(@Param("issueId") int issueId, @Param("accepted") boolean accepted, @Param("fixer") String fixer,
            @Param("fixed") Date fixed);

    List<RecIssue> retrieveIssues(@Param("articleId") int articleId);

    List<RecIssue> retrieveUserIssues(@Param("user") String user);

    List<RecIssue> retrieveUserOpenIssues(@Param("user") String user);

    List<RecIssue> retrieveAuthorIssues(@Param("user") String user);

    RecIssue getIssue(@Param("issueId") int issueId);
}
