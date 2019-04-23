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

    String validate(Header header, String articleType, int articleId, String aticleHeader, byte[] xml) throws Exception;

    String validateAll(Header header, String articleType) throws Exception;

    void reassignUsers(Header header, String articleType, int[] articleIds, String[] users) throws Exception;

    List<ArticleFull> getAllArticles(Header header, String articleType) throws Exception;

    void addArticles(Header header, String articleType, ArticleFull[] articles) throws Exception;

    String preparePreview(Header header, String articleType, String articleHeader, byte[] xml) throws Exception;

    String[] preparePreviews(Header header, String articleType, int[] articleIds) throws Exception;

    List<Related> listIssues(Header header) throws Exception;

    List<Related> listNews(Header header) throws Exception;
}
