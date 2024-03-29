package org.im.dc.server.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.im.dc.service.dto.ArticlesFilter;

/**
 * Interface for declare DB access statements.
 */
public interface DoArticle {
    List<RecArticle> listArticles(@Param("articleType") String articleType, @Param("filter") ArticlesFilter filter);

    List<RecArticle> getAllArticles(@Param("articleType") String articleType);

    RecArticle selectArticle(@Param("id") int id);

    List<RecArticle> selectArticles(@Param("ids") int[] ids);

    RecArticle selectArticleForUpdate(@Param("id") int id);

    List<RecArticle> selectLinkedTo(@Param("header") String header);

    void insertArticle(@Param("record") RecArticle rec);

    void insertArticles(@Param("records") List<RecArticle> list);

    int updateArticle(@Param("record") RecArticle rec, @Param("prevLastUpdated") Date prevLastUpdated);

    int updateArticleHeaderXml(@Param("record") RecArticle rec);

    int updateArticleHeaders(@Param("record") RecArticle rec);

    void reassignArticles(@Param("articleIds") int[] articleIds, @Param("users") String[] users);

    void assignArticles(@Param("articleIds") int[] articleIds, @Param("user") String user);

    void unassignArticles(@Param("articleIds") int[] articleIds, @Param("user") String user);

    int updateArticleState(@Param("record") RecArticle rec, @Param("prevLastUpdated") Date prevLastUpdated);

    void addWatch(@Param("articleId") int articleId, @Param("user") String user);

    void removeWatch(@Param("articleId") int articleId, @Param("user") String user);

    List<Integer> selectAllIds(@Param("articleType") String articleType);
}
