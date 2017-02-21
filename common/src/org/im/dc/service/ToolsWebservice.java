package org.im.dc.service;

import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

@WebService
public interface ToolsWebservice {
    InitialData getInitialData(Header header);

    void getStatistics();

    void validateAll();

    void reassignUsers();

    void addWords(Header header, String[] users, String[] words, String initialState) throws Exception;

    String printPreview(Header header, int articleId) throws Exception;

    List<Related> listIssues(Header header) throws Exception;

    List<Related> listNews(Header header) throws Exception;
}
