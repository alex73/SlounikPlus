package org.im.dc.server.db;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.im.dc.server.handlers.StringArrayTypeHandler;

/**
 * Interface for declare DB access statements.
 */
public interface DoArticle {
    @Select("SELECT * FROM Articles WHERE articleId = #{id} FOR UPDATE")
    @Results({ @Result(property = "words", column = "words", typeHandler = StringArrayTypeHandler.class),
            @Result(property = "assignedUsers", column = "assignedUsers", typeHandler = StringArrayTypeHandler.class),
            @Result(property = "markers", column = "markers", typeHandler = StringArrayTypeHandler.class),
            @Result(property = "watchers", column = "watchers", typeHandler = StringArrayTypeHandler.class),
            @Result(property = "linkedTo", column = "linkedTo", typeHandler = StringArrayTypeHandler.class) })
    RecArticle selectArticle(int id);

    @Select("SELECT articleId, words FROM Articles WHERE linkedTo && ARRAY[#{array,typeHandler=StringArrayTypeHandler}]::varchar[][]")
    @Results({ @Result(property = "words", column = "words", typeHandler = StringArrayTypeHandler.class) })
    List<RecArticle> selectLinkedTo(String[] words);

    @Insert("INSERT INTO Articles (words,xml,assignedUsers,state,markers,watchers,linkedTo,textForSearch,lettersCount,lastUpdated) "
            + "VALUES(#{words,typeHandler=StringArrayTypeHandler},#{xml},#{assignedUsers,typeHandler=StringArrayTypeHandler},#{state},#{markers,typeHandler=StringArrayTypeHandler},#{watchers,typeHandler=StringArrayTypeHandler},#{linkedTo,typeHandler=StringArrayTypeHandler},#{textForSearch},#{lettersCount},#{lastUpdated})")
    @Options(useGeneratedKeys = true, keyProperty = "articleId")
    void insertArticle(RecArticle rec);

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
