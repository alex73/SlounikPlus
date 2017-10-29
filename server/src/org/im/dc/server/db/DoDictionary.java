package org.im.dc.server.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface DoDictionary {
    List<RecDictionary> getDictionaries();

    void addDictionaries(@Param("items") List<RecDictionary> list);
}
