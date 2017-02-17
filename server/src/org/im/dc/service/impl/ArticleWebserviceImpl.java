package org.im.dc.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecComment;
import org.im.dc.service.AppConst;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleHistoryShort;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;

@WebService(endpointInterface = "org.im.dc.service.ArticleWebservice")
public class ArticleWebserviceImpl implements ArticleWebservice {

    private void check(Header header) {
        if (header.appVersion != AppConst.APP_VERSION) {
            throw new RuntimeException("Wrong app version");
        }
        if (!Config.checkUser(header.user, header.pass)) {
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public ArticleFullInfo getArticleFullInfo(Header header, int articleId) {
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(articleId));
        if (rec == null) {
            return null;
        }

        ArticleFullInfo a = new ArticleFullInfo();
        a.article = new ArticleFull();
        a.article.id = rec.getArticleId();
        a.article.words = rec.getWords();
        a.article.xml = rec.getXml();
        a.article.state = rec.getState();
        a.article.markers = rec.getMarkers();
        a.article.assignedUsers = rec.getAssignedUsers();
        a.article.notes = rec.getNotes();
        a.article.lastUpdated = rec.getLastUpdated();

        return a;
    }

    @Override
    public ArticleFullInfo saveArticle(Header header, ArticleFull article) {
        check(header);

        Db.exec((api) -> {
            RecArticle rec = api.getArticleMapper().selectArticle(article.id);
            if (rec == null) {
                throw new RuntimeException("No record in db");
            }
            rec.setXml(article.xml);
            rec.setNotes(article.notes);
            rec.setLastUpdated(new Date());
            int u = api.getArticleMapper().updateArticle(rec, article.lastUpdated);
            if (u != 1) {
                throw new RuntimeException("No updated. Possible somebody other");
            }
        });

        return getArticleFullInfo(header, article.id);
    }

    @Override
    public ArticleFullInfo changeState(Header header, int articleId, String newState, Date lastUpdated) {
        check(header);

        Db.exec((api) -> {
            RecArticle rec = api.getArticleMapper().selectArticle(articleId);
            if (rec == null) {
                throw new RuntimeException("No record in db");
            }
            rec.setState(newState);
            rec.setLastUpdated(new Date());
            int u = api.getArticleMapper().updateArticle(rec, lastUpdated);
            if (u != 1) {
                throw new RuntimeException("No updated. Possible somebody other");
            }
        });

        return getArticleFullInfo(header, articleId);
    }

    @Override
    public void addComment(Header header, int articleId, String comment) {
        check(header);

        Db.exec((api) -> {
            RecComment rec = new RecComment();
            rec.setArticleId(articleId);
            rec.setCreated(new Date());
            rec.setAuthor(header.user);
            rec.setComment(comment);
            api.getCommentMapper().insertComment(rec);
        });
    }

    @Override
    public void addIssue(Header header, int articleId, String issueText, byte[] currentXml, byte[] proposedXml) {
    }

    @Override
    public void fixIssue(Header header, int articleId, int issueId, boolean accepted) {
    }

    @Override
    public void setWatch(Header header, int articleId, boolean watch) {
    }

    @Override
    public List<ArticleShort> listArticles(Header header, ArticlesFilter filter) {
        check(header);

        List<ArticleShort> result = new ArrayList<>();
        List<RecArticle> list = Db.execAndReturn((api) -> api.getSession().selectList("listArticles", filter));
        for (RecArticle r : list) {
            ArticleShort o = new ArticleShort();
            o.id = r.getArticleId();
            o.state = r.getState();
            o.words = r.getWords();
            result.add(o);
        }

        return result;
    }

    @Override
    public List<ArticleHistoryShort> listTodo(Header header) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> listNews(Header header) {
        // TODO Auto-generated method stub
        return null;
    }
}
