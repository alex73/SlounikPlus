package org.im.dc.server.startup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.service.dto.ArticlesFilter;

public class ExportToGit {
    static File repoPath;
    static Git git;
    static List<RecArticleHistory> history;
    static Map<Integer, String> words = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init();

        repoPath = new File("/tmp/dictrepo");
        FileUtils.deleteDirectory(repoPath);

        // create the directory
        git = Git.init().setDirectory(repoPath).call();

        Db.exec((api) -> {
            for (RecArticle a : api.getArticleMapper().listArticles(new ArticlesFilter())) {
                words.put(a.getArticleId(), Arrays.toString(a.getWords()));
            }
            history = api.getArticleHistoryMapper().retrieveAllHistory();
        });

        Collections.sort(history, new Comparator<RecArticleHistory>() {
            @Override
            public int compare(RecArticleHistory o1, RecArticleHistory o2) {
                return Long.compare(o2.getHistoryId(), o1.getHistoryId());
            }
        });

        for (RecArticleHistory h : history) {
            if (h.getOldWords() != null) {
                words.put(h.getArticleId(), Arrays.toString(h.getOldWords()));
            }
        }

        Collections.sort(history, new Comparator<RecArticleHistory>() {
            @Override
            public int compare(RecArticleHistory o1, RecArticleHistory o2) {
                return Long.compare(o1.getHistoryId(), o2.getHistoryId());
            }
        });

        for (RecArticleHistory h : history) {
            if (h.getNewWords() != null) {
                words.put(h.getArticleId(), Arrays.toString(h.getNewWords()));
            }
            if (h.getNewXml() != null) {
                add(h);
            }
        }
    }

    static void add(RecArticleHistory h) {
        try {
            String xml = xml2text(h.getNewXml());
            FileUtils.writeStringToFile(new File(repoPath, h.getArticleId() + ".xml"), xml, StandardCharsets.UTF_8);
            git.add().addFilepattern(h.getArticleId() + ".xml").call();

            int offset = TimeZone.getDefault().getOffset(h.getChanged().getTime());

            // and then commit the changes
            CommitCommand cc = git.commit();
            PersonIdent pi = new PersonIdent(h.getChanger(), "user@localhost", h.getChanged().getTime(), offset);
            cc.setCommitter(pi).setMessage(words.get(h.getArticleId())).call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private static String xml2text(byte[] xml) throws Exception {
        if (xml == null) {
            return "";
        }
        Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamResult res = new StreamResult(new StringWriter());
        Source sou = new StreamSource(new ByteArrayInputStream(xml));
        transformer.transform(sou, res);
        return res.getWriter().toString();
    }
}