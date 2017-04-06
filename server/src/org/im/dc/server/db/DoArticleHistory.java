package org.im.dc.server.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface DoArticleHistory {
    List<RecArticleHistory> retrieveUserHistory(@Param("user") String user);

    List<RecArticleHistory> retrieveHistory(@Param("articleId") int articleId);

    RecArticleHistory getHistory(@Param("historyId") int historyId);

    void insertArticleHistory(@Param("hist") RecArticleHistory hist);
}
