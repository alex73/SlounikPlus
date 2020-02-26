package org.im.dc.service;

import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

@WebService
public interface ToolsWebservice {
    InitialData getInitialData(Header header) throws Exception;

    void getStatistics(Header header) throws Exception;

    void reassignUsers(Header header, String articleType, int[] articleIds, String[] users) throws Exception;

    List<ArticleFull> getAllArticles(Header header, String articleType) throws Exception;

    void addArticles(Header header, String articleType, ArticleFull[] articles) throws Exception;

    String validate(Header header, String articleType, int articleId, byte[] xml) throws Exception;

    OutputSummaryStorage previewValidateAll(Header header, String articleType) throws Exception;

    OutputSummaryStorage preparePreview(Header header, String articleType, int articleId, byte[] xml) throws Exception;

    OutputSummaryStorage preparePreviews(Header header, String articleType, int[] articleIds) throws Exception;

    List<Related> listIssues(Header header) throws Exception;

    List<Related> listNews(Header header) throws Exception;
}
