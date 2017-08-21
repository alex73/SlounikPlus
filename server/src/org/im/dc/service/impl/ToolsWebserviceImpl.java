package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.jws.WebService;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.Permission;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.User;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.PermissionChecker;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.server.db.RecComment;
import org.im.dc.server.db.RecIssue;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.AppConst;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "org.im.dc.service.ToolsWebservice")
public class ToolsWebserviceImpl implements ToolsWebservice {
    private static final Logger LOG = LoggerFactory.getLogger(ToolsWebserviceImpl.class);

    private void check(Header header) {
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
    public InitialData getInitialData(Header header) {
        LOG.info(">> getInitialData(" + header.user + ")");
        header.configVersion = Config.getConfig().getVersion(); // set version for initial client call
        check(header);

        InitialData result = new InitialData();
        result.configVersion = Config.getConfig().getVersion();
        result.articleSchema = Config.articleSchemaSource;
        result.states = new ArrayList<>();
        for (State st : Config.getConfig().getStates().getState()) {
            result.states.add(st.getId());
        }
        result.allUsers = new TreeMap<>();
        for (User u : Config.getConfig().getUsers().getUser()) {
            result.allUsers.put(u.getName(), u.getRole());
        }
        result.currentUserRole = PermissionChecker.getUserRole(header.user);
        result.currentUserPermissions = PermissionChecker.getUserPermissions(header.user);
        result.newArticleState = PermissionChecker.getUserNewArticleState(header.user);
        result.newArticleUsers = PermissionChecker.getUserNewArticleUsers(header.user);

        LOG.info("<< getInitialData");
        return result;
    }

    @Override
    public void getStatistics(Header header) {
    }

    @Override
    public String validate(Header header, int articleId, String[] words, byte[] xml) throws Exception {
        LOG.info(">> validate(" + header.user + ")");
        check(header);

        String err;
        try {
            RecArticle a = new RecArticle();
            a.setArticleId(articleId);
            a.setWords(words);
            a.setXml(xml);
            err = ArticleWebserviceImpl.validateArticle(a);
        } catch (Exception ex) {
            throw new RuntimeException("Памылка ў артыкуле #" + articleId);
        }

        LOG.info("<< validate");
        return err;
    }

    @Override
    public void validateAll(Header header) throws Exception {
        LOG.info(">> validateAll(" + header.user + ")");
        check(header);
        PermissionChecker.userRequiresPermission(header.user, Permission.FULL_VALIDATION);

        List<Integer> articleIds = Db.execAndReturn((api) -> api.getArticleMapper().selectAllIds());

        for (int id : articleIds) {
            Db.exec((api) -> {
                RecArticle a = api.getArticleMapper().selectArticleForUpdate(id);
                String err;
                try {
                    err = ArticleWebserviceImpl.validateArticle(a);
                } catch (Exception ex) {
                    throw new RuntimeException("Памылка ў артыкуле #" + id);
                }
                Date prevUpdated = a.getLastUpdated();
                a.setValidationError(err);
                a.setLastUpdated(new Date());
                int u = api.getArticleMapper().updateArticle(a, prevUpdated);
                if (u != 1) {
                    LOG.info("<< validateAll: db was not updated");
                    throw new RuntimeException("No updated. Possible somebody other updated");
                }
            });
        }

        LOG.info("<< validateAll");
    }

    @Override
    public void reassignUsers(Header header, int[] articleIds, String[] users) throws Exception {
        LOG.info(">> reassignUsers(" + header.user + ")");
        check(header);
        PermissionChecker.userRequiresPermission(header.user, Permission.REASSIGN);

        Db.exec((api) -> {
            api.getArticleMapper().reassignArticles(articleIds, users);
        });

        LOG.info("<< reassignUsers");
    }

    @Override
    public void addWords(Header header, String[] users, String[] words, String initialState) throws Exception {
        LOG.info(">> addWords(" + header.user + ")");
        check(header);
        PermissionChecker.userRequiresPermission(header.user, Permission.ADD_WORDS);

        Date lastUpdated = new Date();
        List<RecArticle> list = new ArrayList<>();
        List<String> checkWords = new ArrayList<>();
        for (String w : words) {
            w = w.trim();
            if (w.isEmpty()) {
                continue;
            }
            String[] wa = w.split(",");
            for (int i = 0; i < wa.length; i++) {
                wa[i] = wa[i].trim();
                if (wa[i].isEmpty()) {
                    LOG.warn("<< addWords: wrong words: " + w);
                    throw new Exception("Wrong words: " + w);
                }
                checkWords.add(wa[i]);
            }
            RecArticle r = new RecArticle();
            r.setAssignedUsers(users);
            r.setWords(wa);
            r.setState(initialState);
            r.setMarkers(new String[0]);
            r.setWatchers(new String[0]);
            r.setLinkedTo(new String[0]);
            r.setLastUpdated(lastUpdated);
            list.add(r);
        }

        Db.exec((api) -> {
            List<RecArticle> existArticles = api.getArticleMapper().getArticlesWithWords(checkWords);
            if (!existArticles.isEmpty()) {
                LOG.warn("<< addWords: already exist " + Arrays.toString(existArticles.get(0).getWords()));
                throw new RuntimeException("Словы ўжо ёсьць: " + Arrays.toString(existArticles.get(0).getWords()));
            }
            api.getArticleMapper().insertArticles(list);
        });

        LOG.info("<< addWords");
    }

    @Override
    public String preparePreview(Header header, String[] words, byte[] xml) throws Exception {
        LOG.info(">> preparePreview(" + header.user + ")");
        check(header);

        Validator validator = Config.articleSchema.newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(xml)));

        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", words, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(xml), ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec(new File(Config.getConfigDir(), "output.js").getAbsolutePath(), context);

        LOG.info("<< preparePreview");
        return out.toString();
    }

    @Override
    public List<Related> listIssues(Header header) throws Exception {
        LOG.info(">> listIssues(" + header.user + ")");
        List<Related> related = new ArrayList<>();
        // заўвагі
        List<RecIssue> list = Db.execAndReturn((api) -> api.getIssueMapper().retrieveUserOpenIssues(header.user));
        for (RecIssue rc : list) {
            related.add(rc.getRelated());
        }
        Related.sortByTimeDesc(related);

        LOG.info("<< listIssues");
        return related;
    }

    @Override
    public List<Related> listNews(Header header) throws Exception {
        LOG.info(">> listNews(" + header.user + ")");
        List<Related> related = new ArrayList<>();
        // заўвагі карыстальніка
        List<RecIssue> list = Db.execAndReturn((api) -> api.getIssueMapper().retrieveAuthorIssues(header.user));
        for (RecIssue rc : list) {
            related.add(rc.getRelated());
        }
        // заўвагі для артыкулаў за якімі сочыць карыстальнік
        list = Db.execAndReturn((api) -> api.getIssueMapper().retrieveUserIssues(header.user));
        for (RecIssue rc : list) {
            related.add(rc.getRelated());
        }
        // камэнтары
        List<RecComment> listComments = Db
                .execAndReturn((api) -> api.getCommentMapper().retrieveUserComments(header.user));
        for (RecComment rc : listComments) {
            related.add(rc.getRelated());
        }
        // гісторыя
        List<RecArticleHistory> listHistory = Db
                .execAndReturn((api) -> api.getArticleHistoryMapper().retrieveUserHistory(header.user));
        for (RecArticleHistory rh : listHistory) {
            related.add(rh.getRelated());
        }
        Related.sortByTimeDesc(related);

        LOG.info("<< listNews");
        return related;
    }
}
