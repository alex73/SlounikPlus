package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.Link;
import org.im.dc.gen.config.Permission;
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
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.Related;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "org.im.dc.service.ArticleWebservice")
public class ArticleWebserviceImpl implements ArticleWebservice {
    private static final Logger LOG = LoggerFactory.getLogger(ArticleWebserviceImpl.class);

    private void check(Header header) throws Exception {
        if (header.appVersion != AppConst.APP_VERSION) {
            LOG.warn("<< getInitialData: version required " + AppConst.APP_VERSION + " but requested "
                    + header.appVersion);
            throw new RuntimeException("Wrong app version");
        }
        if (!PermissionChecker.checkUser(header.user, header.pass)) {
            LOG.warn("<< getInitialData: wrong user/pass");
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public ArticleFullInfo getArticleFullInfo(Header header, int articleId) throws Exception {
        LOG.info(">> getArticleFullInfo: " + articleId);
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(articleId));
        if (rec == null) {
            LOG.warn("<< getArticleFullInfo: there is no specified article");
            throw new Exception("Няма вызначанага артыкула");
        }
        preprocessArticle(null, rec);

        ArticleFullInfo a = getAdditionalArticleInfo(header, rec);
        LOG.info("<< getArticleFullInfo: " + articleId);
        return a;
    }

    private void preprocessArticle(Db.Api api, RecArticle rec) throws Exception {
        if (rec.getXml() != null) {
            Validator validator = Config.articleSchema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(rec.getXml())));
        }
        // TODO call script for check article
    }

    private ArticleFullInfo getAdditionalArticleInfo(Header header, RecArticle rec) throws Exception {
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
                .execAndReturn((api) -> api.getArticleHistoryMapper().retrieveHistory(rec.getArticleId()))) {
            a.related.add(rh.getRelated());
        }
        // камэнтары
        for (RecComment rc : Db.execAndReturn((api) -> api.getCommentMapper().retrieveComments(rec.getArticleId()))) {
            a.related.add(rc.getRelated());
        }
        // заўвагі
        for (RecIssue rc : Db.execAndReturn((api) -> api.getIssueMapper().retrieveIssues(rec.getArticleId()))) {
            a.related.add(rc.getRelated());
        }
        Related.sortByTimeDesc(a.related);

        for (RecArticle linked : Db.execAndReturn((api) -> api.getArticleMapper().selectLinkedTo(a.article.words))) {
            ArticleFullInfo.LinkFrom lf = new ArticleFullInfo.LinkFrom();
            lf.articleId = linked.getArticleId();
            lf.words = linked.getWords();
            a.linksFrom.add(lf);
        }

        if (a.article.words.length > 0) {
            for (Link link : Config.getConfig().getExternalLinks().getLink()) {
                ArticleFullInfo.LinkExternal ef = new ArticleFullInfo.LinkExternal();
                ef.name = link.getName();
                ef.url = link.getValue().replace("{}", a.article.words[0]);
                a.linksExternal.add(ef);
            }
        }

        return a;
    }

    @Override
    public ArticleFullInfo saveArticle(Header header, ArticleFull article) throws Exception {
        LOG.info(">> saveArticle");
        check(header);

        RecArticle art = Db.execAndReturn((api) -> {
            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec = api.getArticleMapper().selectArticle(article.id);
            if (rec == null) {
                LOG.warn("<< saveArticle: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(article.lastUpdated)) {
                LOG.info("<< saveArticle: lastUpdated was changed");
                throw new RuntimeException("Possible somebody other updated");
            }
            PermissionChecker.canUserEditArticle(header.user, rec.getState());

            history.setArticleId(rec.getArticleId());
            history.setOldXml(rec.getXml());

            rec.setXml(article.xml);
            rec.setNotes(article.notes);
            rec.setLastUpdated(currentDate);

            preprocessArticle(api, rec);

            int u = api.getArticleMapper().updateArticle(rec, article.lastUpdated);
            if (u != 1) {
                LOG.info("<< saveArticle: db was not updated");
                throw new RuntimeException("No updated. Possible somebody other updated");
            }

            history.setNewXml(rec.getXml());
            history.setChanged(currentDate);
            history.setChanger(header.user);
            if (!Arrays.equals(history.getOldXml(), history.getNewXml())) {
                // запіс у гісторыю - толькі калі зьмяніўся XML, а не нататкі
                api.getArticleHistoryMapper().insertArticleHistory(history);
            }

            return rec;
        });

        ArticleFullInfo a = getAdditionalArticleInfo(header, art);
        LOG.info("<< saveArticle");
        return a;
    }

    @Override
    public ArticleFullInfo changeWords(Header header, int articleId, String newWords, Date lastUpdated)
            throws Exception {
        LOG.info(">> changeWords");
        check(header);

        List<String> ws = new ArrayList<>();
        for (String w : newWords.split(",")) {
            w = w.trim();
            if (!w.isEmpty()) {
                ws.add(w);
            }
        }

        if (ws.isEmpty()) {
            throw new RuntimeException("Пустыя словы");
        }

        PermissionChecker.userRequiresPermission(header.user, Permission.ADD_WORDS);

        RecArticle art = Db.execAndReturn((api) -> {
            List<RecArticle> existArticles = api.getSession().selectList("hasArticlesWithWords", ws);
            for (RecArticle e : existArticles) {
                if (e.getArticleId() != articleId) {
                    // the same article
                    LOG.warn("<< changeWords: already exist " + Arrays.toString(e.getWords()));
                    throw new RuntimeException("Словы ўжо ёсьць: " + Arrays.toString(e.getWords()));
                }
            }

            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec = api.getArticleMapper().selectArticle(articleId);
            if (rec == null) {
                LOG.warn("<< changeWords: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                LOG.info("<< changeWords: lastUpdated was changed");
                throw new RuntimeException("Possible somebody other updated");
            }

            history.setArticleId(rec.getArticleId());
            history.setOldWords(rec.getWords());

            rec.setWords(ws.toArray(new String[ws.size()]));
            rec.setLastUpdated(currentDate);

            int u = api.getArticleMapper().updateWords(rec, lastUpdated);
            if (u != 1) {
                LOG.info("<< changeWords: db was not updated");
                throw new RuntimeException("No updated. Possible somebody other updated");
            }

            history.setNewWords(rec.getWords());
            history.setChanged(currentDate);
            history.setChanger(header.user);
            api.getArticleHistoryMapper().insertArticleHistory(history);

            return rec;
        });

        ArticleFullInfo a = getAdditionalArticleInfo(header, art);
        LOG.info("<< changeWords");
        return a;
    }

    @Override
    public ArticleFullInfo addIssue(Header header, int articleId, String issueText, byte[] proposedXml,
            Date lastUpdated) throws Exception {
        LOG.info(">> addIssue");
        check(header);

        RecArticle art = Db.execAndReturn((api) -> {
            RecArticle rec = api.getArticleMapper().selectArticle(articleId);
            if (rec == null) {
                LOG.warn("<< addIssue: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                LOG.info("<< addIssue: lastUpdated was changed");
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

            return rec;
        });

        ArticleFullInfo a = getAdditionalArticleInfo(header, art);
        LOG.info("<< addIssue");
        return a;
    }

    @Override
    public ArticleFullInfo changeState(Header header, int articleId, String newState, Date lastUpdated)
            throws Exception {
        LOG.info(">> changeState");
        check(header);

        RecArticle art = Db.execAndReturn((api) -> {
            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec = api.getArticleMapper().selectArticle(articleId);
            if (rec == null) {
                LOG.warn("<< changeState: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                LOG.info("<< changeState: lastUpdated was changed");
                throw new RuntimeException("Possible somebody other updated");
            }
            PermissionChecker.canUserChangeArticleState(header.user, rec.getState(), newState);

            history.setArticleId(rec.getArticleId());
            history.setOldState(rec.getState());

            rec.setState(newState);
            rec.setLastUpdated(new Date());
            int u = api.getArticleMapper().updateArticleState(rec, lastUpdated);
            if (u != 1) {
                LOG.info("<< changeState: db was not updated");
                throw new RuntimeException("No updated. Possible somebody other");
            }

            history.setNewState(rec.getState());
            history.setChanged(currentDate);
            history.setChanger(header.user);
            api.getArticleHistoryMapper().insertArticleHistory(history);

            return rec;
        });

        ArticleFullInfo a = getAdditionalArticleInfo(header, art);
        LOG.info("<< changeState");
        return a;
    }

    @Override
    public ArticleFullInfo addComment(Header header, int articleId, String comment) throws Exception {
        LOG.info(">> addComment");
        check(header);

        Db.exec((api) -> {
            RecComment rec = new RecComment();
            rec.setArticleId(articleId);
            rec.setCreated(new Date());
            rec.setAuthor(header.user);
            rec.setComment(comment);
            api.getCommentMapper().insertComment(rec);
        });

        LOG.info("<< addComment");
        return getArticleFullInfo(header, articleId);
    }

    @Override
    public void fixIssue(Header header, int articleId, int issueId, boolean accepted) {
    }

    @Override
    public void setWatch(Header header, int articleId, boolean watch) throws Exception {
        LOG.info(">> setWatch");
        check(header);

        Db.exec((api) -> {
            if (watch) {
                api.getArticleMapper().addWatch(articleId, header.user);
            } else {
                api.getArticleMapper().removeWatch(articleId, header.user);
            }
        });

        LOG.info("<< setWatch");
    }

    @Override
    public List<ArticleShort> listArticles(Header header, ArticlesFilter filter) throws Exception {
        LOG.info(">> listArticles");
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

        LOG.info("<< listArticles");
        return result;
    }
}
