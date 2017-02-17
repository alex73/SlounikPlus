package org.im.dc.service;

import javax.jws.WebService;

import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;

@WebService
public interface ToolsWebservice {
    InitialData getInitialData(Header header);

    void getStatistics();

    void validateAll();

    void reassignUsers();

    void addWords();

    String printPreview(Header header, int articleId) throws Exception;
}
