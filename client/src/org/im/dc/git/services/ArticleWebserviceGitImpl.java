package org.im.dc.git.services;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.client.GitProc;
import org.im.dc.config.PermissionChecker;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.ValidationHelper;
import org.im.dc.service.dto.ArticleCommentFull;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleHistoryFull;
import org.im.dc.service.dto.ArticleIssueFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.Header;
import org.w3c.dom.Document;

public class ArticleWebserviceGitImpl implements ArticleWebservice {
    @Override
    public ArticleFullInfo getArticleFullInfo(Header header, String articleTypeId, int articleId) throws Exception {
        Path file = GitProc.getInstance().getPathById(articleId);
        ArticleFullInfo a = new ArticleFullInfo();
        a.article = new ArticleFull();
        a.article.id = articleId;
        a.article.type = articleTypeId;
        a.article.header = file.toString().replaceAll("[^/]+/(.+)\\.xml", "$1");
        a.article.xml = Files.readAllBytes(file);

        a.youCanEdit = PermissionChecker.canUserEditArticle(ToolsWebserviceGitImpl.config, header.user, articleTypeId,
                null, null);

        ValidationHelper helper = validateArticle(a.article.type, a.article.xml);
        a.article.validationError = helper.error;

        return a;
    }

    @Override
    public ArticleFullInfo saveArticle(Header header, ArticleFull article) throws Exception {
        Path oldPath = getArticleFile(article.type, article.header);
        byte[] prev = null;
        if (oldPath != null) {
            prev = Files.readAllBytes(oldPath);
        }

        GitProc.getInstance().reset();

        byte[] newp = null;
        if (oldPath != null) {
            newp = Files.readAllBytes(oldPath);
        }
        if (!Arrays.equals(prev, newp)) {
            throw new Exception("Нехта іншы змяніў артыкул - немагчыма захаваць");
        }

        ValidationHelper helper = validateArticle(article.type, article.xml);
        boolean headerChanged = false;
        if (helper.newHeader != null) {
            headerChanged = !helper.newHeader.equals(article.header);
            article.header = helper.newHeader;
        }
        if (!headerChanged && Arrays.equals(newp, article.xml)) {
            // no changes
            return getArticleFullInfo(header, article.type, article.id);
        }
        synchronized (GitProc.getInstance()) {
            if (headerChanged && oldPath != null) {
                GitProc.getInstance().remove(oldPath);
            }

            Path newPath = getArticleFile(article.type, article.header);
            Files.createDirectories(newPath.getParent());
            Files.write(newPath, article.xml);
            GitProc.getInstance().add(newPath);

            GitProc.getInstance().commit(header.user, "user@slounik.plus");
            
            article.id = GitProc.getInstance().getId(newPath);
        }

        return getArticleFullInfo(header, article.type, article.id);
    }

    protected Path getArticleFile(String articleTypeId, String header) {
        if (header == null) {
            return null;
        }
        return GitProc.getInstance().getLocalDir().resolve(articleTypeId).resolve(header + ".xml");
    }

    @Override
    public ArticleFullInfo changeState(Header header, String articleType, int articleId, String newState,
            Date lastUpdated) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public ArticleFullInfo addComment(Header header, String articleType, int articleId, String comment)
            throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public ArticleFullInfo addIssue(Header header, String articleType, int articleId, String issueText,
            byte[] proposedXml, Date lastUpdated) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public ArticleFullInfo fixIssue(Header header, String articleType, int articleId, int issueId, boolean accepted)
            throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public void setWatch(Header header, String articleType, int articleId, boolean watch) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public List<ArticleShort> listArticles(Header header, String articleType, ArticlesFilter filter) throws Exception {
        List<ArticleShort> result = new ArrayList<>();
        Path dir = GitProc.getInstance().getLocalDir().resolve(articleType);
        if (Files.isDirectory(dir)) {
            Files.find(dir, 1000, (p, a) -> Files.isRegularFile(p)).sorted().forEach(p -> {
                String fn = dir.relativize(p).toString();
                ArticleShort o = new ArticleShort();
                o.id = GitProc.getInstance().getId(p);
                o.header = fn;
                result.add(o);
            });
        }
        return result;
    }

    @Override
    public ArticleCommentFull getComment(Header header, int commentId) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public ArticleIssueFull getIssue(Header header, int issueId) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    @Override
    public ArticleHistoryFull getHistory(Header header, int historyId) throws Exception {
        throw new Exception("Not implemented for git version");
    }

    protected static ValidationHelper validateArticle(String articleTypeId, byte[] xml) {
        if (xml == null) {
            return null;
        }
        Validator validator = GitProc.configSchemas.get(articleTypeId).newValidator();
        ValidationHelper helper = new ValidationHelper(0, validator, xml);
        try {
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("helper", helper, ScriptContext.ENGINE_SCOPE);
            Document doc = JsDomWrapper.parseDoc(xml);
            context.setAttribute("articleDoc", doc, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("article", new JsDomWrapper(doc.getDocumentElement()), ScriptContext.ENGINE_SCOPE);
            JsProcessing.exec(GitProc.getInstance().getLocalDir().resolve(articleTypeId + "-validate.js").toString(),
                    context);
        } catch (ScriptException ex) {
            helper.error = ex.getCause().getMessage();
        } catch (Exception ex) {
            ex.printStackTrace();
            helper.error = "Памылка валідацыі артыкула: " + ex.getMessage();
        }
        return helper;
    }
}
