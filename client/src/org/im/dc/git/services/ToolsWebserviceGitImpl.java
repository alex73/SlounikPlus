package org.im.dc.git.services;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.im.dc.client.GitProc;
import org.im.dc.config.ConfigLoad;
import org.im.dc.gen.config.Config;
import org.im.dc.service.AppConst;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.ValidationHelper;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

public class ToolsWebserviceGitImpl implements ToolsWebservice {
    static Config config;

    @Override
    public InitialData getInitialData(Header header) throws Exception {
        config = ConfigLoad.loadConfig(new File(GitProc.getInstance().getLocalDir().toFile(), "config.xml"));
        if (!Integer.toString(AppConst.APP_VERSION).equals(config.getAppVersion())) {
            throw new Exception("Config has wrong app version");
        }
        Map<String, byte[]> schemaSources = ConfigLoad.loadSchemaSources(GitProc.getInstance().getLocalDir().toFile());
        InitialData result = ConfigLoad.config2initialData(config, header.user, schemaSources);
        GitProc.configSchemas = ConfigLoad.loadSchemas(schemaSources, config.getTypes().getType());
        return result;
    }

    @Override
    public void getStatistics(Header header) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public String validate(Header header, String articleTypeId, int articleId, String aticleHeader, byte[] xml)
            throws Exception {
        try {
            ValidationHelper helper = ArticleWebserviceGitImpl.validateArticle(articleTypeId, xml);
            return helper.error;
        } catch (Exception ex) {
            throw new RuntimeException("Памылка ў артыкуле #" + articleId);
        }
    }

    @Override
    public void validateAll(Header header) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public void reassignUsers(Header header, String articleType, int[] articleIds, String[] users) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public List<ArticleFull> getAllArticles(Header header, String articleType) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public void addArticles(Header header, String articleType, ArticleFull[] articles) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public String preparePreview(Header header, String articleType, String articleHeader, byte[] xml) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public String[] preparePreviews(Header header, String articleType, int[] articleIds) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public List<Related> listIssues(Header header) throws Exception {
        return null;
    }

    @Override
    public List<Related> listNews(Header header) throws Exception {
        return null;
    }
}
