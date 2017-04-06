package org.im.dc.server.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.im.dc.server.handlers.StringArrayTypeHandler;
import org.im.dc.service.dto.ArticlesFilter;

/**
 * Interface for declare DB access statements.
 */
public interface DoArticle {
    List<RecArticle> listArticles(@Param("filter") ArticlesFilter filter);

    RecArticle selectArticleForUpdate(@Param("id") int id);

    List<RecArticle> selectLinkedTo(@Param("words") String[] words);

    void insertArticle(@Param("record") RecArticle rec);

    void insertArticles(@Param("records") List<RecArticle> list);

    @Update("UPDATE Articles SET xml = #{0.xml}," + " words = #{0.words,typeHandler=StringArrayTypeHandler},"
            + " assignedUsers = #{0.assignedUsers,typeHandler=StringArrayTypeHandler}," + " state = #{0.state},"
            + " markers = #{0.markers,typeHandler=StringArrayTypeHandler},"
            + " watchers = #{0.watchers,typeHandler=StringArrayTypeHandler},"
            + " linkedTo = #{0.linkedTo,typeHandler=StringArrayTypeHandler},"
            + " textForSearch = #{0.textForSearch}, lettersCount = #{0.lettersCount}, lastUpdated = #{0.lastUpdated} "
            + " WHERE articleId = #{0.articleId} AND lastUpdated = #{1}")
    int updateArticle(RecArticle rec, Date prevLastUpdated);

    @Update("UPDATE Articles SET words = #{0.words,typeHandler=StringArrayTypeHandler}, lastUpdated = #{0.lastUpdated} "
            + " WHERE articleId = #{0.articleId} AND lastUpdated = #{1}")
    int updateWords(RecArticle rec, Date prevLastUpdated);

    @Update("UPDATE Articles SET state = #{0.state}, lastUpdated = #{0.lastUpdated} "
            + " WHERE articleId = #{0.articleId} AND lastUpdated = #{1}")
    int updateArticleState(RecArticle rec, Date prevLastUpdated);

    @Update("UPDATE Articles SET watchers = array_append(watchers, #{1}) WHERE articleId = #{0} AND NOT (watchers @> ARRAY[#{1}]::varchar[][])")
    void addWatch(int articleId, String user);

    @Update("UPDATE Articles SET watchers = array_remove(watchers, #{1}) WHERE articleId = #{0} AND (watchers @> ARRAY[#{1}]::varchar[][])")
    void removeWatch(int articleId, String user);

    @Select("SELECT * FROM Articles")
    @Results({ @Result(property = "words", column = "words", typeHandler = StringArrayTypeHandler.class) })
    List<RecArticle> selectAll();
}
