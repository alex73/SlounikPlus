package org.im.dc.service;

import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.Dictionaries;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

@WebService
public interface ToolsWebservice {
    InitialData getInitialData(Header header);

    void getStatistics(Header header) throws Exception;

    String validate(Header header, int articleId, String[] words, byte[] xml) throws Exception;

    void validateAll(Header header) throws Exception;

    void reassignUsers(Header header, int[] articleIds, String[] users) throws Exception;

    void addWords(Header header, String[] users, String[] words, String initialState) throws Exception;

    String preparePreview(Header header, String[] words, byte[] xml) throws Exception;

    List<Related> listIssues(Header header) throws Exception;

    List<Related> listNews(Header header) throws Exception;

    Dictionaries getDictionaries(Header header) throws Exception;

    void addDictionaries(Header header, Dictionaries newValues) throws Exception;
}
