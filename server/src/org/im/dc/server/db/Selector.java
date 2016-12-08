package org.im.dc.server.db;

import org.apache.ibatis.annotations.Select;

/**
 * Interface for declare DB access statements.
 */
public interface Selector {
    @Select("SELECT * FROM Articles WHERE ID = #{id}")
    RecArticle selectArticle(int id);
}
