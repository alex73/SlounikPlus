package org.im.dc.service;

import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.RelatedMany;

@WebService
public interface ToolsWebservice {
    InitialData getInitialData(Header header);

    void getStatistics();

    void validateAll();

    void reassignUsers();

    void addWords(Header header, String[] users, String[] words, String initialState) throws Exception;

    String printPreview(Header header, int articleId) throws Exception;

    List<RelatedMany> listTodo(Header header) throws Exception;

    List<RelatedMany> listNews(Header header) throws Exception;
}
