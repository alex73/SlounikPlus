package org.im.dc.service.impl;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jws.WebService;
import javax.script.ScriptException;

import org.im.dc.config.ConfigLoad;
import org.im.dc.config.PermissionChecker;
import org.im.dc.gen.config.CommonPermission;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.VersionChecker;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.server.db.RecComment;
import org.im.dc.server.db.RecIssue;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;
import org.im.dc.service.impl.js.JsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "org.im.dc.service.ToolsWebservice")
public class ToolsWebserviceImpl implements ToolsWebservice {
    private static final Logger LOG = LoggerFactory.getLogger(ToolsWebserviceImpl.class);

    private void check(Header header) throws Exception {
        VersionChecker.check(header);
        if (!PermissionChecker.checkUser(Config.getConfig(), header.user, header.pass)) {
            LOG.warn("<< check: wrong user/pass");
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public InitialData getInitialData(Header header) throws Exception {
        LOG.info(">> getInitialData(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        InitialData result = ConfigLoad.config2initialData(Config.getConfig(), header.user, Config.schemaSources);

        LOG.info("<< getInitialData (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }

    @Override
    public byte[] getFile(String path) throws Exception {
        LOG.info(">> getFile(" + path + ")");
        Path dir = Paths.get(Config.getConfigDir());
        if (path == null) {
            List<String> result;
            try (Stream<Path> files = Files.find(dir, Integer.MAX_VALUE, (p, a) -> a.isRegularFile()).sorted()) {
                result = files.map(p -> p.toString()).collect(Collectors.toList());
            }
            return String.join("\n", result).getBytes();
        } else {
            Path f = dir.resolve(path);
            if (!f.toAbsolutePath().toString().startsWith(dir.toAbsolutePath().toString())) {
                return null;
            }
            try {
                return Files.readAllBytes(f);
            } catch (FileNotFoundException ex) {
                return null;
            }
        }
    }

    @Override
    public void getStatistics(Header header) throws Exception {
    }

    @Override
    public void reassignUsers(Header header, String articleType, int[] articleIds, String[] users) throws Exception {
        LOG.info(">> reassignUsers(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(Config.getConfig(), header.user, articleType, TypePermission.REASSIGN);

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
    public void assignUser(Header header, String articleType, int[] articleIds, String username) throws Exception {
        LOG.info(">> assignUser(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(Config.getConfig(), header.user, articleType, TypePermission.REASSIGN);

        Db.exec((api) -> {
            checkArticlesType(api, articleType, articleIds);
            api.getArticleMapper().assignArticles(articleIds, username);
        });

        LOG.info("<< assignUser (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    @Override
    public void unassignUser(Header header, String articleType, int[] articleIds, String username) throws Exception {
        LOG.info(">> unassignUser(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(Config.getConfig(), header.user, articleType, TypePermission.REASSIGN);

        Db.exec((api) -> {
            checkArticlesType(api, articleType, articleIds);
            api.getArticleMapper().unassignArticles(articleIds, username);
        });

        LOG.info("<< unassignUser (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    protected void checkArticlesType(Db.Api db, String articleType, int[] articleIds) {
        for (int articleId : articleIds) {
            RecArticle a = db.getArticleMapper().selectArticle(articleId);
            if (!articleType.equals(a.getArticleType())) {
                LOG.warn("<< validateAll: wrong type/id requested");
                throw new RuntimeException("Запыт няправільнага ID для вызначанага тыпу");
            }
        }
    }

    public List<ArticleFull> getAllArticles(Header header, String articleType) throws Exception {
        LOG.info(">> getAllArticles(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        List<ArticleFull> result = new ArrayList<>();
        Db.exec((api) -> {
            List<RecArticle> existArticles = api.getArticleMapper().getAllArticles(articleType);
            for (RecArticle a : existArticles) {
                ArticleFull r = new ArticleFull();
                r.id = a.getArticleId();
                r.type = a.getArticleType();
                r.header = a.getHeader();
                r.xml = a.getXml();
                r.state = a.getState();
                r.markers = a.getMarkers();
                r.assignedUsers = a.getAssignedUsers();
                r.lastUpdated = a.getLastUpdated();
                r.validationError = a.getValidationError();
                result.add(r);
            }
        });

        LOG.info("<< getAllArticles (" + (System.currentTimeMillis() - startTime) + "ms)");
        return result;
    }

    @Override
    public void addArticles(Header header, String articleType, ArticleFull[] articles) throws Exception {
        LOG.info(">> addArticles(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(Config.getConfig(), header.user, articleType, TypePermission.ADD_ARTICLES);
        Date lastUpdated = new Date();
        List<RecArticle> list = new ArrayList<>();
        Set<String> newHeaders = new HashSet<>();
        String initialState = PermissionChecker.getNewArticleState(Config.getConfig(), articleType);
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

    /**
     * Validate only one article during read/write for edit.
     */
    @Override
    public String validate(Header header, String articleType, int articleId, byte[] xml) throws Exception {
        LOG.info(">> validate(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);

        String err;
        try {
            RecArticle a = new RecArticle();
            a.setArticleId(articleId);
            a.setArticleType(articleType);
            a.setXml(xml);

            OutputSummaryStorage storage = JsHelper.previewSomeArticles(articleType, Arrays.asList(a));
            List<String> errors = storage.errors.stream().filter(e -> e.articleId == a.getArticleId()).map(e -> e.error)
                    .collect(Collectors.toList());
            err = errors.isEmpty() ? null : String.join("\n", errors);
        } catch (Exception ex) {
            LOG.error("validate error", ex);
            throw new RuntimeException("Памылка праверкі артыкула", ex);
        }

        LOG.info("<< validate (" + (System.currentTimeMillis() - startTime) + "ms)");
        return err;
    }

    /**
     * Prepare preview only one article from GUI.
     */
    @Override
    public OutputSummaryStorage preparePreview(Header header, String articleType, int articleId, byte[] xml) throws Exception {
        LOG.info(">> preparePreview(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(Config.getConfig(), header.user, articleType,
                TypePermission.VIEW_OUTPUT);

        try {
            RecArticle a = new RecArticle();
            a.setArticleId(articleId);
            a.setArticleType(articleType);
            a.setXml(xml);

            OutputSummaryStorage storage = JsHelper.previewSomeArticles(articleType, Arrays.asList(a));

            LOG.info("<< preparePreview (" + (System.currentTimeMillis() - startTime) + "ms)");
            return storage;
        } catch (ScriptException ex) {
            ex.printStackTrace();
            throw new Exception(ex.getCause().getMessage());
        } catch (Exception ex) {
            LOG.error("Error in preparePreview", ex);
            throw ex;
        }
    }

    /**
     * Prepare preview for several articles.
     */
    @Override
    public OutputSummaryStorage preparePreviews(Header header, String articleType, int[] articleIds) throws Exception {
        LOG.info(">> preparePreviews(" + header.user + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresTypePermission(Config.getConfig(), header.user, articleType,
                TypePermission.VIEW_OUTPUT);

        try {
            List<RecArticle> todo = Db.execAndReturn((api) -> {
                List<RecArticle> r = new ArrayList<>();
                List<RecArticle> articles = api.getArticleMapper().selectArticles(articleIds);
                for (RecArticle a : articles) {
                    if (a == null) {
                        // no such article
                        continue;
                    }
                    if (!articleType.equals(a.getArticleType())) {
                        LOG.warn("<< preparePreviews: wrong type/id requested");
                        throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
                    }
                    r.add(a);
                }
                return r;
            });

            OutputSummaryStorage storage = JsHelper.previewSomeArticles(articleType, todo);
            JsHelper.validateSummary(articleType, storage);

            LOG.info("<< preparePreviews (" + (System.currentTimeMillis() - startTime) + "ms)");
            return storage;
        } catch (Exception ex) {
            LOG.error("Error in preparePreviews", ex);
            throw ex;
        }
    }

    /**
     * Prepare preview for all articles, save validation errors for each article, then validate summaries.
     */
    @Override
    public OutputSummaryStorage previewValidateAll(Header header, String articleType) throws Exception {
        LOG.info(">> validateAll(" + header.user + "," + articleType + ")");
        long startTime = System.currentTimeMillis();
        check(header);
        PermissionChecker.userRequiresCommonPermission(Config.getConfig(), header.user, CommonPermission.FULL_VALIDATION);

        OutputSummaryStorage storage;
        try {
            List<RecArticle> todo = Db.execAndReturn((api) -> {
                List<RecArticle> r = new ArrayList<>();
                List<RecArticle> articles = api.getArticleMapper().getAllArticles(articleType);
                for (RecArticle a : articles) {
                    if (a == null) {
                        // no such article
                        continue;
                    }
                    if (!articleType.equals(a.getArticleType())) {
                        LOG.warn("<< preparePreviews: wrong type/id requested");
                        throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
                    }
                    r.add(a);
                }
                return r;
            });
            LOG.info("   validateAll - preview for each article started");
            storage = JsHelper.previewSomeArticles(articleType, todo);
            LOG.info("   validateAll - update each article started");
            for (RecArticle a : todo) {
                Db.execBatch((api) -> {
                    List<String> errors = storage.errors.stream().filter(e -> e.articleId == a.getArticleId()).map(e -> e.error).collect(Collectors.toList());
                    a.setValidationError(errors.isEmpty() ? null : String.join("\n", errors));
                    a.setHeader(storage.headers.get(a.getArticleId()));
                    String[] linkedTo = storage.linkedTo.get(a.getArticleId());
                    if (linkedTo != null) {
                        for (int i = 0; i < linkedTo.length; i++) {
                            linkedTo[i] = linkedTo[i].trim().replace("+", "").replaceAll("/.+", "");
                        }
                        a.setLinkedTo(linkedTo);
                    } else {
                        a.setLinkedTo(new String[0]);
                    }
                    a.setTextForSearch(storage.textForSearch.get(a.getArticleId()));

                    api.getArticleMapper().updateArticleHeaders(a);
                });
            }
            LOG.info("   validateAll - summary validation start");
            JsHelper.validateSummary(articleType, storage);
        } catch (Exception ex) {
            LOG.error("Error in validateAll", ex);
            throw ex;
        }
        LOG.info("<< validateAll (" + (System.currentTimeMillis() - startTime) + "ms)");
        return storage;
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
