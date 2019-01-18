package org.im.dc.service;

import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import org.im.dc.service.dto.ArticleCommentFull;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleHistoryFull;
import org.im.dc.service.dto.ArticleIssueFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;

@WebService
public interface ArticleWebservice {
    ArticleFullInfo getArticleFullInfo(Header header, String articleType, int articleId) throws Exception;

    ArticleFullInfo saveArticle(Header header, ArticleFull article, boolean batchUpdate) throws Exception;

    ArticleFullInfo changeState(Header header, String articleType, int articleId, String newState, Date lastUpdated)
            throws Exception;

    ArticleFullInfo addComment(Header header, String articleType, int articleId, String comment) throws Exception;

    ArticleFullInfo addIssue(Header header, String articleType, int articleId, String issueText, byte[] proposedXml,
            Date lastUpdated) throws Exception;

    ArticleFullInfo fixIssue(Header header, String articleType, int articleId, int issueId, boolean accepted)
            throws Exception;

    void setWatch(Header header, String articleType, int articleId, boolean watch) throws Exception;

    List<ArticleShort> listArticles(Header header, String articleType, ArticlesFilter filter) throws Exception;

    ArticleCommentFull getComment(Header header, int commentId) throws Exception;

    ArticleIssueFull getIssue(Header header, int issueId) throws Exception;

    ArticleHistoryFull getHistory(Header header, int historyId) throws Exception;
}
