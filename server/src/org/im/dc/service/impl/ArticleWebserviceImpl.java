package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.PermissionChecker;
import org.im.dc.server.VersionChecker;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.server.db.RecArticleNote;
import org.im.dc.server.db.RecComment;
import org.im.dc.server.db.RecIssue;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.dto.ArticleCommentFull;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleHistoryFull;
import org.im.dc.service.dto.ArticleIssueFull;
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
        VersionChecker.check(header);
        if (!PermissionChecker.checkUser(header.user, header.pass)) {
            LOG.warn("<< check: wrong user/pass");
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public ArticleFullInfo getArticleFullInfo(Header header, String articleType, int articleId) throws Exception {
        LOG.info(">> getArticleFullInfo(" + header.user + "): " + articleId);
        long startTime = System.currentTimeMillis();
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(articleId));
        if (rec == null) {
            LOG.warn("<< getArticleFullInfo: there is no specified article");
            throw new Exception("Няма вызначанага артыкула");
        }
        String err = validateArticle(rec);
        rec.setValidationError(err);

        ArticleFullInfo a = getAdditionalArticleInfo(header, rec);
        if (!articleType.equals(a.article.type)) {
            LOG.warn("<< getArticleFullInfo: wrong type/id requested");
            throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
        }
        LOG.info("<< getArticleFullInfo: " + articleId + " (" + (System.currentTimeMillis() - startTime) + "ms)");
        return a;
    }

    protected static String validateArticle(RecArticle rec) {
        if (rec.getXml() == null) {
            return null;
        }
        String result = null;
        Validator validator = Config.schemas.get(rec.getArticleType()).xsdSchema.newValidator();
        ValidationHelper helper = new ValidationHelper(rec.getArticleId());
        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(rec.getXml())));

            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("helper", helper, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("header", rec.getHeader(), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("article", new JsDomWrapper(rec.getXml()), ScriptContext.ENGINE_SCOPE);
            JsProcessing.exec(new File(Config.getConfigDir(), rec.getArticleType() + "-validate.js").getAbsolutePath(),
                    context);
        } catch (ScriptException ex) {
            result = ex.getCause().getMessage();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Памылка валідацыі артыкула: " + ex.getMessage();
        }
        if (helper.newHeader != null) {
            rec.setHeader(helper.newHeader);
        }
        rec.setLinkedTo(helper.getLinks());
        rec.setTextForSearch(new WordSplitter().parse(rec.getXml()));

        return result;
    }

    private ArticleFullInfo getAdditionalArticleInfo(Header header, RecArticle rec) throws Exception {
        ArticleFullInfo a = new ArticleFullInfo();
        a.article = new ArticleFull();
        a.article.id = rec.getArticleId();
        a.article.type = rec.getArticleType();
        a.article.header = rec.getHeader();
        a.article.xml = rec.getXml();
        a.article.state = rec.getState();
        a.article.markers = rec.getMarkers();
        a.article.assignedUsers = rec.getAssignedUsers();
        a.article.lastUpdated = rec.getLastUpdated();
        a.article.validationError = rec.getValidationError();

        a.youCanEdit = PermissionChecker.canUserEditArticle(header.user, rec.getArticleType(), rec.getState(), rec.getAssignedUsers());
        a.youWatched = false;
        for (String w : rec.getWatchers()) {
            if (header.user.equals(w)) {
                a.youWatched = true;
                break;
            }
        }
        // нататкі
        RecArticleNote note = Db
                .execAndReturn((api) -> api.getArticleNoteMapper().getNote(rec.getArticleId(), header.user));
        if (note != null) {
            a.article.notes = note.getNote();
        }
        // гісторыя
        List<RecArticleHistory> history = Db
                .execAndReturn((api) -> api.getArticleHistoryMapper().retrieveHistory(rec.getArticleId()));
        for (RecArticleHistory rh : history) {
            a.related.add(rh.getRelated());
        }
        // камэнтары
        for (RecComment rc : Db
                .execAndReturn((api) -> api.getCommentMapper().retrieveArticleComments(rec.getArticleId()))) {
            a.related.add(rc.getRelated());
        }
        // заўвагі
        for (RecIssue rc : Db.execAndReturn((api) -> api.getIssueMapper().retrieveIssues(rec.getArticleId()))) {
            a.related.add(rc.getRelated());
        }
        Related.sortByTimeDesc(a.related);

        /*
         * for (RecArticle linked : Db.execAndReturn((api) -> api.getArticleMapper().selectLinkedTo(a.article.header)))
         * { ArticleFullInfo.LinkFrom lf = new ArticleFullInfo.LinkFrom(); lf.articleId = linked.getArticleId();
         * lf.header = linked.getHeader(); a.linksFrom.add(lf); }
         */

        return a;
    }

    @Override
    public ArticleFullInfo saveArticle(Header header, ArticleFull article) throws Exception {
        LOG.info(">> saveArticle(" + header.user + "): " + article.id);
        long startTime = System.currentTimeMillis();
        check(header);

        RecArticle art = Db.execAndReturn((api) -> {
            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec;
            if (article.id != 0) {
                rec = api.getArticleMapper().selectArticleForUpdate(article.id);
                if (rec == null) {
                    LOG.warn("<< saveArticle: no record in db");
                    throw new RuntimeException("No record in db");
                }
                if (!rec.getLastUpdated().equals(article.lastUpdated)) {
                    LOG.info("<< saveArticle: lastUpdated was changed");
                    throw new RuntimeException("Possible somebody other updated");
                }
            } else {
                // new article
                rec = new RecArticle();
                rec.setArticleType(article.type);
                rec.setState(PermissionChecker.getNewArticleState(article.type));
                rec.setMarkers(new String[0]);
                rec.setWatchers(new String[0]);
            }
            if (!PermissionChecker.canUserEditArticle(header.user, rec.getArticleType(), rec.getState(), rec.getAssignedUsers())) {
                throw new RuntimeException("Permission error: user can't change article");
            }

            history.setOldXml(rec.getXml());
            history.setOldHeader(rec.getHeader());

            rec.setXml(article.xml);
            rec.setLastUpdated(currentDate);

            String err = validateArticle(rec);
            rec.setValidationError(err);

            if (article.id != 0) {
                int u = api.getArticleMapper().updateArticle(rec, article.lastUpdated);
                if (u != 1) {
                    LOG.info("<< saveArticle: db was not updated");
                    throw new RuntimeException("No updated. Possible somebody other updated");
                }
            } else {
                api.getArticleMapper().insertArticle(rec);
            }

            api.getArticleNoteMapper().deleteNote(rec.getArticleId(), header.user);
            if (article.notes != null && !article.notes.trim().isEmpty()) {
                RecArticleNote note = new RecArticleNote();
                note.setArticleId(rec.getArticleId());
                note.setCreator(header.user);
                note.setNote(article.notes);
                api.getArticleNoteMapper().insertNote(note);
            }

            history.setArticleId(rec.getArticleId());
            history.setNewHeader(rec.getHeader());
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
        LOG.info("<< saveArticle (" + (System.currentTimeMillis() - startTime) + "ms)");
        return a;
    }

    @Override
    public ArticleFullInfo addIssue(Header header, String articleType, int articleId, String issueText,
            byte[] proposedXml, Date lastUpdated) throws Exception {
        LOG.info(">> addIssue(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        RecArticle art = Db.execAndReturn((api) -> {
            RecArticle rec = api.getArticleMapper().selectArticleForUpdate(articleId);
            if (rec == null) {
                LOG.warn("<< addIssue: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                LOG.info("<< addIssue: lastUpdated was changed");
                throw new RuntimeException("Possible somebody other updated");
            }
            if (!articleType.equals(rec.getArticleType())) {
                LOG.warn("<< addIssue: wrong type/id requested");
                throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
            }

            RecIssue issue = new RecIssue();
            issue.setArticleId(rec.getArticleId());
            issue.setCreated(new Date());
            issue.setAuthor(header.user);
            issue.setComment(issueText);
            issue.setOldXml(proposedXml != null ? rec.getXml() : null);
            issue.setNewXml(proposedXml);

            api.getIssueMapper().insertIssue(issue);

            return rec;
        });

        ArticleFullInfo a = getAdditionalArticleInfo(header, art);
        LOG.info("<< addIssue (" + (System.currentTimeMillis() - startTime) + "ms)");
        return a;
    }

    @Override
    public ArticleFullInfo changeState(Header header, String articleType, int articleId, String newState,
            Date lastUpdated) throws Exception {
        LOG.info(">> changeState(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        RecArticle art = Db.execAndReturn((api) -> {
            Date currentDate = new Date();
            RecArticleHistory history = new RecArticleHistory();

            RecArticle rec = api.getArticleMapper().selectArticleForUpdate(articleId);
            if (rec == null) {
                LOG.warn("<< changeState: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!rec.getLastUpdated().equals(lastUpdated)) {
                LOG.info("<< changeState: lastUpdated was changed");
                throw new RuntimeException("Possible somebody other updated");
            }
            if (!articleType.equals(rec.getArticleType())) {
                LOG.warn("<< changeState: wrong type/id requested");
                throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
            }
            if (!PermissionChecker.canUserChangeArticleState(header.user, rec.getArticleType(), rec.getState(), newState)) {
                throw new RuntimeException("Permission error: Impossible state change");
            }

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
        LOG.info("<< changeState (" + (System.currentTimeMillis() - startTime) + "ms)");
        return a;
    }

    @Override
    public ArticleFullInfo addComment(Header header, String articleType, int articleId, String comment)
            throws Exception {
        LOG.info(">> addComment(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        Db.exec((api) -> {
            RecArticle reca = api.getArticleMapper().selectArticle(articleId);
            if (reca == null) {
                LOG.warn("<< addComment: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!articleType.equals(reca.getArticleType())) {
                LOG.warn("<< addComment: wrong type/id requested");
                throw new RuntimeException("Запыт няправільнага ID для вызначанага тыпу");
            }
            RecComment rec = new RecComment();
            rec.setArticleId(articleId);
            rec.setCreated(new Date());
            rec.setAuthor(header.user);
            rec.setComment(comment);
            api.getCommentMapper().insertComment(rec);
        });

        LOG.info("<< addComment (" + (System.currentTimeMillis() - startTime) + "ms)");
        return getArticleFullInfo(header, articleType, articleId);
    }

    @Override
    public ArticleFullInfo fixIssue(Header header, String articleType, int articleId, int issueId, boolean accepted)
            throws Exception {
        LOG.info(">> fixIssue(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        Db.exec((api) -> {
            RecArticle reca = api.getArticleMapper().selectArticle(articleId);
            if (reca == null) {
                LOG.warn("<< fixIssue: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!articleType.equals(reca.getArticleType())) {
                LOG.warn("<< fixIssue: wrong type/id requested");
                throw new RuntimeException("Запыт няправільнага ID для вызначанага тыпу");
            }
            api.getIssueMapper().fixIssue(issueId, accepted, header.user, new Date());
        });

        LOG.info("<< fixIssue (" + (System.currentTimeMillis() - startTime) + "ms)");
        return getArticleFullInfo(header, articleType, articleId);
    }

    @Override
    public void setWatch(Header header, String articleType, int articleId, boolean watch) throws Exception {
        LOG.info(">> setWatch(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        Db.exec((api) -> {
            RecArticle reca = api.getArticleMapper().selectArticle(articleId);
            if (reca == null) {
                LOG.warn("<< setWatch: no record in db");
                throw new RuntimeException("No record in db");
            }
            if (!articleType.equals(reca.getArticleType())) {
                LOG.warn("<< setWatch: wrong type/id requested");
                throw new RuntimeException("Запыт няправільнага ID для вызначанага тыпу");
            }
            if (watch) {
                api.getArticleMapper().addWatch(articleId, header.user);
            } else {
                api.getArticleMapper().removeWatch(articleId, header.user);
            }
        });

        LOG.info("<< setWatch (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    @Override
    public List<ArticleShort> listArticles(Header header, String articleType, ArticlesFilter filter) throws Exception {
        LOG.info(">> listArticles(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        List<ArticleShort> result = new ArrayList<>();
        List<RecArticle> list = Db.execAndReturn((api) -> api.getArticleMapper().listArticles(articleType, filter));
        for (RecArticle r : list) {
            ArticleShort o = new ArticleShort();
            o.id = r.getArticleId();
            o.type = r.getArticleType();
            o.state = r.getState();
            o.header = r.getHeader();
            o.assignedUsers = r.getAssignedUsers();
            o.validationError = r.getValidationError();
            result.add(o);
        }
        ArticleShort.sortById(result);

        LOG.info("<< listArticles (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }

    @Override
    public ArticleCommentFull getComment(Header header, int commentId) throws Exception {
        LOG.info(">> getComment(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        RecComment rc = Db.execAndReturn((api) -> api.getCommentMapper().getComment(commentId));
        ArticleCommentFull result;
        if (rc == null) {
            result = null;
        } else {
            result = new ArticleCommentFull();
            result.comment = rc.getComment();
            result.who = rc.getAuthor();
            result.when = rc.getCreated();
        }
        LOG.info("<< getComment (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }

    @Override
    public ArticleIssueFull getIssue(Header header, int issueId) throws Exception {
        LOG.info(">> getIssue(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        RecIssue rc = Db.execAndReturn((api) -> api.getIssueMapper().getIssue(issueId));
        ArticleIssueFull result;
        if (rc == null) {
            result = null;
        } else {
            result = new ArticleIssueFull();
            result.comment = rc.getComment();
            result.oldXml = rc.getOldXml();
            result.newXml = rc.getNewXml();
            result.who = rc.getAuthor();
            result.when = rc.getCreated();
            result.fixed = rc.getFixed();
            result.fixer = rc.getFixer();
        }
        LOG.info("<< getIssue (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }

    @Override
    public ArticleHistoryFull getHistory(Header header, int historyId) throws Exception {
        LOG.info(">> getHistory(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        RecArticleHistory rc = Db.execAndReturn((api) -> api.getArticleHistoryMapper().getHistory(historyId));
        ArticleHistoryFull result;
        if (rc == null) {
            result = null;
        } else {
            result = new ArticleHistoryFull();
            result.oldXml = rc.getOldXml();
            result.newXml = rc.getNewXml();
            result.who = rc.getChanger();
            result.when = rc.getChanged();
        }
        LOG.info("<< getHistory (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }
}
