package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jws.WebService;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.CommonPermission;
import org.im.dc.gen.config.Type;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.gen.config.User;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.PermissionChecker;
import org.im.dc.server.VersionChecker;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.server.db.RecComment;
import org.im.dc.server.db.RecIssue;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "org.im.dc.service.ToolsWebservice")
public class ToolsWebserviceImpl implements ToolsWebservice {
    private static final Logger LOG = LoggerFactory.getLogger(ToolsWebserviceImpl.class);

    private void check(Header header) throws Exception {
        VersionChecker.check(header);
        if (!PermissionChecker.checkUser(header.user, header.pass)) {
            LOG.warn("<< check: wrong user/pass");
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public InitialData getInitialData(Header header) throws Exception {
        LOG.info(">> getInitialData(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        InitialData result = new InitialData();
        result.configVersion = Config.getConfig().getVersion();
        result.headerLocale = Config.getConfig().getHeaderLocale();
        result.stress = Config.getConfig().getStress();
        for (Type type : Config.getConfig().getTypes().getType()) {
            InitialData.TypeInfo ti = new InitialData.TypeInfo();
            ti.typeId = type.getId();
            ti.typeName = type.getName();
            ti.newArticleState = PermissionChecker.getNewArticleState(ti.typeId);
            ti.articleSchema = Config.schemas.get(type.getId()).source;
            result.articleTypes.add(ti);
        }
        result.currentUserPermissions = PermissionChecker.getUserPermissions(header.user);
        Map<String, Set<String>> ps = PermissionChecker.getUserPermissionsByType(header.user);
        for (Type type : Config.getConfig().getTypes().getType()) {
            Set<String> permissionsList = ps.get(type.getId());
            if (permissionsList != null) {
                result.getTypeInfo(type.getId()).currentUserTypePermissions.addAll(permissionsList);
            }
        }
        result.states.addAll(Config.getConfig().getStates().getState());
        result.allUsers = new TreeMap<>();
        for (User u : Config.getConfig().getUsers().getUser()) {
            result.allUsers.put(u.getName(), u.getRole());
        }
        result.currentUserRole = PermissionChecker.getUserRole(header.user);

        LOG.info("<< getInitialData (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }

    @Override
    public void getStatistics(Header header) throws Exception {
    }

    @Override
    public String validate(Header header, String articleType, int articleId, String articleHeader, byte[] xml)
            throws Exception {
        LOG.info(">> validate(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        String err;
        try {
            RecArticle a = new RecArticle();
            a.setArticleId(articleId);
            a.setArticleType(articleType);
            a.setHeader(articleHeader);
            a.setXml(xml);
            err = ArticleWebserviceImpl.validateArticle(a);
        } catch (Exception ex) {
            throw new RuntimeException("Памылка ў артыкуле #" + articleId);
        }

        LOG.info("<< validate (" + (System.currentTimeMillis() - startTime) + "ms)");
        return err;
    }

    @Override
    public void validateAll(Header header) throws Exception {
        LOG.info(">> validateAll(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresCommonPermission(header.user, CommonPermission.FULL_VALIDATION);

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

        LOG.info("<< validateAll (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    @Override
    public void reassignUsers(Header header, String articleType, int[] articleIds, String[] users) throws Exception {
        LOG.info(">> reassignUsers(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(header.user, articleType, TypePermission.REASSIGN);

        Db.exec((api) -> {
            for (int articleId : articleIds) {
                RecArticle a = api.getArticleMapper().selectArticle(articleId);
                if (!articleType.equals(a.getArticleType())) {
                    LOG.warn("<< validateAll: wrong type/id requested");
                    throw new RuntimeException("Запыт няправільнага ID для вызначанага тыпу");
                }
            }
            api.getArticleMapper().reassignArticles(articleIds, users);
        });

        LOG.info("<< reassignUsers (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    @Override
    public void addArticles(Header header, String articleType, ArticleFull[] articles) throws Exception {
        LOG.info(">> addArticles(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(header.user, articleType, TypePermission.ADD_ARTICLES);
        Date lastUpdated = new Date();
        List<RecArticle> list = new ArrayList<>();
        Set<String> newHeaders = new HashSet<>();
        String initialState = PermissionChecker.getNewArticleState(articleType);
        for (ArticleFull a : articles) {
            newHeaders.add(a.header);
            RecArticle r = new RecArticle();
            r.setArticleType(articleType);
            r.setAssignedUsers(a.assignedUsers);
            r.setHeader(a.header);
            r.setState(initialState);
            r.setXml(a.xml);
            r.setMarkers(new String[0]);
            r.setWatchers(new String[0]);
            r.setLinkedTo(new String[0]);
            r.setLastUpdated(lastUpdated);
            list.add(r);
        }

        Db.exec((api) -> {
            List<RecArticle> existArticles = api.getArticleMapper().listArticles(articleType, new ArticlesFilter());
            for (RecArticle a : existArticles) {
                if (newHeaders.contains(a.getHeader())) {
                    LOG.warn("<< addArticles: already exist: " + a.getHeader());
                    throw new RuntimeException("Артыкул ўжо існуе: " + a.getHeader());
                }
            }
            // insert by 1000 records chunks
            for (int fromIndex = 0; fromIndex < list.size(); fromIndex += 1000) {
                int toIndex = Math.min(fromIndex + 1000, list.size());
                api.getArticleMapper().insertArticles(list.subList(fromIndex, toIndex));
            }
        });

        LOG.info("<< addArticles (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    @Override
    public String preparePreview(Header header, String articleType, String articleHeader, byte[] xml) throws Exception {
        LOG.info(">> preparePreview(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(header.user, articleType, TypePermission.VIEW_OUTPUT);

        try {
            Validator validator = Config.schemas.get(articleType).xsdSchema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xml)));

            HtmlOut out = new HtmlOut();
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("header", articleHeader, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("article", new JsDomWrapper(xml), ScriptContext.ENGINE_SCOPE);
            JsProcessing.exec(new File(Config.getConfigDir(), "output.js").getAbsolutePath(), context);

            LOG.info("<< preparePreview (" + (System.currentTimeMillis() - startTime) + "ms)");
            return out.toString();
        } catch (Exception ex) {
            LOG.error("Error in preparePreview", ex);
            throw ex;
        }
    }

    @Override
    public String[] preparePreviews(Header header, String articleType, int[] articleIds) throws Exception {
        LOG.info(">> preparePreviews(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(header.user, articleType, TypePermission.VIEW_OUTPUT);

        try {
            Map<Integer, RecArticle> articlesMap = new HashMap<>();
            Db.exec((api) -> {
                List<RecArticle> articles = api.getArticleMapper().selectArticles(articleIds);
                articles.forEach(a -> articlesMap.put(a.getArticleId(), a));
            });

            String[] result = new String[articleIds.length];
            for (int i = 0; i < articleIds.length; i++) {
                RecArticle a = articlesMap.get(articleIds[i]);
                if (a == null) {
                    // no such article
                    continue;
                }
                if (!articleType.equals(a.getArticleType())) {
                    LOG.warn("<< preparePreviews: wrong type/id requested");
                    throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
                }
                Validator validator = Config.schemas.get(articleType).xsdSchema.newValidator();
                validator.validate(new StreamSource(new ByteArrayInputStream(a.getXml())));

                HtmlOut out = new HtmlOut();
                SimpleScriptContext context = new SimpleScriptContext();
                context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
                context.setAttribute("header", a.getHeader(), ScriptContext.ENGINE_SCOPE);
                context.setAttribute("article", new JsDomWrapper(a.getXml()), ScriptContext.ENGINE_SCOPE);
                JsProcessing.exec(new File(Config.getConfigDir(), "output.js").getAbsolutePath(), context);
                result[i] = out.toString();
            }

            LOG.info("<< preparePreviews (" + (System.currentTimeMillis() - startTime) + "ms)");
            return result;
        } catch (Exception ex) {
            LOG.error("Error in preparePreviews", ex);
            throw ex;
        }
    }

    @Override
    public List<Related> listIssues(Header header) throws Exception {
        LOG.info(">> listIssues(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        List<Related> related = new ArrayList<>();
        // заўвагі
        List<RecIssue> list = Db.execAndReturn((api) -> api.getIssueMapper().retrieveUserOpenIssues(header.user));
        for (RecIssue rc : list) {
            related.add(rc.getRelated());
        }
        Related.sortByTimeDesc(related);

        LOG.info("<< listIssues (" + (System.currentTimeMillis() - startTime) + "ms)");
        return related;
    }

    @Override
    public List<Related> listNews(Header header) throws Exception {
        LOG.info(">> listNews(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

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

        LOG.info("<< listNews (" + (System.currentTimeMillis() - startTime) + "ms)");
        return related;
    }
}
