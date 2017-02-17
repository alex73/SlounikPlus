package org.im.dc.service;

import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleHistoryShort;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.Header;

@WebService
public interface ArticleWebservice {
    ArticleFullInfo getArticleFullInfo(Header header, int articleId);

    ArticleFullInfo saveArticle(Header header, ArticleFull article);

    ArticleFullInfo changeState(Header header, int articleId, String newState, Date lastUpdated);

    void addComment(Header header, int articleId, String comment);

    void addIssue(Header header, int articleId, String issueText, byte[] currentXml, byte[] proposedXml);

    void fixIssue(Header header, int articleId, int issueId, boolean accepted);

    void setWatch(Header header, int articleId, boolean watch);

    List<ArticleShort> listArticles(Header header, String state);

    List<ArticleHistoryShort> listTodo(Header header);

    List<Object> listNews(Header header);
}
