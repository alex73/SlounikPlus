package org.im.dc.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.State;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.PermissionChecker;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.server.db.RecComment;
import org.im.dc.server.db.RecIssue;
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

    private void check(Header header) throws Exception {
        if (header.appVersion != AppConst.APP_VERSION) {
            throw new RuntimeException("Wrong app version");
        }
        if (!PermissionChecker.checkUser(header.user, header.pass)) {
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public ArticleFullInfo getArticleFullInfo(Header header, int articleId) throws Exception {
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(articleId));
        if (rec == null) {
            throw new Exception("Няма вызначанага артыкула");
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

        String userRole = PermissionChecker.getUserRole(header.user);
        State state = Config.getStateByName(rec.getState());
        if (state == null) {
            a.youCanEdit = false;
        } else {
            a.youCanEdit = PermissionChecker.roleInRolesList(userRole, state.getEditRoles());
            for (Change ch : state.getChange()) {
                if (PermissionChecker.roleInRolesList(userRole, ch.getRoles())) {
                    a.youCanChangeStateTo.add(ch.getTo());
                }
            }
        }
        a.youWatched = false;
        for (String w : rec.getWatchers()) {
            if (header.user.equals(w)) {
                a.youWatched = true;
                break;
            }
        }
        // гісторыя
        for (RecArticleHistory rh : Db
                .execAndReturn((api) -> api.getArticleHistoryMapper().retrieveHistory(articleId))) {
            ArticleFullInfo.Related h = new ArticleFullInfo.Related();
            h.historyId = rh.getHistoryId();
            h.when = rh.getChanged();
            h.who = rh.getChanger();
            if (rh.getOldState() != null && rh.getNewState() != null) {
                h.what = rh.getOldState() + " -> " + rh.getNewState();
            } else if (rh.getNewXml() != null) {
                h.what = "Тэкст артыкула";
            }
            a.related.add(h);
        }
        // камэнтары
        for (RecComment rc : Db.execAndReturn((api) -> api.getCommentMapper().retrieveComments(articleId))) {
            ArticleFullInfo.Related h = new ArticleFullInfo.Related();
            h.commentId = rc.getCommentId();
            h.when = rc.getCreated();
            h.who = rc.getAuthor();
            h.what = rc.getComment();
            a.related.add(h);
        }
        // заўвагі
        for (RecIssue rc : Db.execAndReturn((api) -> api.getIssueMapper().retrieveIssues(articleId))) {
            ArticleFullInfo.Related h = new ArticleFullInfo.Related();
            h.issueId = rc.getIssueId();
            h.when = rc.getCreated();
            h.who = rc.getAuthor();
            h.what = (rc.isAccepted() ? "done:" : "open:") + rc.getComment();
            a.related.add(h);
        }

        return a;
    }

    @Override
    public ArticleFullInfo saveArticle(Header header, ArticleFull article) throws Exception {
        check(header);

        Db.exec((api) -> {
            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec = api.getArticleMapper().selectArticle(article.id);
            if (rec == null) {
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(article.lastUpdated)) {
                throw new RuntimeException("Possible somebody other updated");
            }
            PermissionChecker.canUserEditArticle(header.user, rec.getState());

            history.setArticleId(rec.getArticleId());
            history.setOldXml(rec.getXml());

            rec.setXml(article.xml);
            rec.setNotes(article.notes);
            rec.setLastUpdated(currentDate);
            int u = api.getArticleMapper().updateArticle(rec, article.lastUpdated);
            if (u != 1) {
                throw new RuntimeException("No updated. Possible somebody other updated");
            }

            history.setNewXml(rec.getXml());
            history.setChanged(currentDate);
            history.setChanger(header.user);
            api.getArticleHistoryMapper().insertArticleHistory(history);
        });

        return getArticleFullInfo(header, article.id);
    }

    @Override
    public ArticleFullInfo addIssue(Header header, int articleId, String issueText, byte[] proposedXml,
            Date lastUpdated) throws Exception {
        check(header);

        Db.exec((api) -> {
            RecArticle rec = api.getArticleMapper().selectArticle(articleId);
            if (rec == null) {
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                throw new RuntimeException("Possible somebody other updated");
            }

            RecIssue issue = new RecIssue();
            issue.setArticleId(rec.getArticleId());
            issue.setCreated(new Date());
            issue.setAuthor(header.user);
            issue.setComment(issueText);
            issue.setOldXml(rec.getXml());
            issue.setNewXml(proposedXml);

            api.getIssueMapper().insertIssue(issue);
        });

        return getArticleFullInfo(header, articleId);
    }

    @Override
    public ArticleFullInfo changeState(Header header, int articleId, String newState, Date lastUpdated)
            throws Exception {
        check(header);

        Db.exec((api) -> {
            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec = api.getArticleMapper().selectArticle(articleId);
            if (rec == null) {
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                throw new RuntimeException("Possible somebody other updated");
            }
            PermissionChecker.canUserChangeArticleState(header.user, rec.getState(), newState);

            history.setArticleId(rec.getArticleId());
            history.setOldState(rec.getState());

            rec.setState(newState);
            rec.setLastUpdated(new Date());
            int u = api.getArticleMapper().updateArticleState(rec, lastUpdated);
            if (u != 1) {
                throw new RuntimeException("No updated. Possible somebody other");
            }

            history.setNewState(rec.getState());
            history.setChanged(currentDate);
            history.setChanger(header.user);
            api.getArticleHistoryMapper().insertArticleHistory(history);
        });

        return getArticleFullInfo(header, articleId);
    }

    @Override
    public ArticleFullInfo addComment(Header header, int articleId, String comment) throws Exception {
        check(header);

        Db.exec((api) -> {
            RecComment rec = new RecComment();
            rec.setArticleId(articleId);
            rec.setCreated(new Date());
            rec.setAuthor(header.user);
            rec.setComment(comment);
            api.getCommentMapper().insertComment(rec);
        });

        return getArticleFullInfo(header, articleId);
    }

    @Override
    public void fixIssue(Header header, int articleId, int issueId, boolean accepted) {
    }

    @Override
    public void setWatch(Header header, int articleId, boolean watch) {
    }

    @Override
    public List<ArticleShort> listArticles(Header header, ArticlesFilter filter) throws Exception {
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
