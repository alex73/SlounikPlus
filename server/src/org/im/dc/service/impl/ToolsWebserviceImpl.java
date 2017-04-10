package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
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
        LOG.info(">> getInitialData");
        check(header);

        InitialData result = new InitialData();
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

        LOG.info("<< getInitialData");
        return result;
    }

    @Override
    public void getStatistics() {
    }

    @Override
    public void validateAll() {
    }

    @Override
    public void reassignUsers() {
    }

    @Override
    public void addWords(Header header, String[] users, String[] words, String initialState) throws Exception {
        LOG.info(">> addWords");
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
            List<RecArticle> existArticles = api.getArticleMapper().hasArticlesWithWords(checkWords);
            if (!existArticles.isEmpty()) {
                LOG.warn("<< addWords: already exist " + Arrays.toString(existArticles.get(0).getWords()));
                throw new RuntimeException("Словы ўжо ёсьць: " + Arrays.toString(existArticles.get(0).getWords()));
            }
            api.getArticleMapper().insertArticles(list);
        });

        LOG.info("<< addWords");
    }

    @Override
    public String printPreview(Header header, int articleId) throws Exception {
        LOG.info(">> printPreview");
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticleForUpdate(articleId));
        if (rec == null) {
            LOG.warn("<< printPreview: article not found");
            return null;
        }

        Validator validator = Config.articleSchema.newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(rec.getXml())));

        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", rec.getWords(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(rec.getXml()), ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("config/output.js", context);

        LOG.info("<< printPreview");
        return out.toString();
    }

    @Override
    public List<Related> listIssues(Header header) throws Exception {
        LOG.info(">> listIssues");
        List<Related> related = new ArrayList<>();
        // заўвагі
        List<RecIssue> list = Db
                .execAndReturn((api) -> api.getIssueMapper().retrieveUserOpenIssues(header.user));
        for (RecIssue rc : list) {
            related.add(rc.getRelated());
        }
        Related.sortByTimeDesc(related);

        LOG.info("<< listIssues");
        return related;
    }

    @Override
    public List<Related> listNews(Header header) throws Exception {
        LOG.info(">> listNews");
        List<Related> related = new ArrayList<>();
        // заўвагі карыстальніка
        List<RecIssue> list = Db
                .execAndReturn((api) -> api.getIssueMapper().retrieveAuthorIssues(header.user));
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
