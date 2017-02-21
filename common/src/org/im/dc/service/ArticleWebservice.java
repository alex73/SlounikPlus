package org.im.dc.service;

import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;

@WebService
public interface ArticleWebservice {
    ArticleFullInfo getArticleFullInfo(Header header, int articleId) throws Exception;

    ArticleFullInfo saveArticle(Header header, ArticleFull article) throws Exception;

    ArticleFullInfo changeState(Header header, int articleId, String newState, Date lastUpdated) throws Exception;

    ArticleFullInfo changeWords(Header header, int articleId, String newWords, Date lastUpdated) throws Exception;

    ArticleFullInfo addComment(Header header, int articleId, String comment) throws Exception;

    ArticleFullInfo addIssue(Header header, int articleId, String issueText, byte[] proposedXml, Date lastUpdated)
            throws Exception;

    void fixIssue(Header header, int articleId, int issueId, boolean accepted);

    void setWatch(Header header, int articleId, boolean watch) throws Exception;

    List<ArticleShort> listArticles(Header header, ArticlesFilter filter) throws Exception;
}
