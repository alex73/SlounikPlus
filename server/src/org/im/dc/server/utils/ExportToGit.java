package org.im.dc.server.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;
import org.im.dc.service.dto.ArticlesFilter;

public class ExportToGit {
    static File repoPath;
    static Git git;
    static List<RecArticleHistory> history;
    static List<GitHistory> gitHistory = new ArrayList<>();
    static Map<Integer, String> headers = new HashMap<>();
    static Map<Integer, String> articleTypes = new HashMap<>();
    static RecArticleHistory fullCurrentHistory;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ExportToGit <git_dir>");
            System.exit(1);
        }

        Db.init(System.getProperty("CONFIG_DIR"));

        repoPath = new File(args[0]);

        try {
            git = Git.open(repoPath);
            Iterable<RevCommit> log = git.log().call();
            for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
                RevCommit rev = iterator.next();
                gitHistory.add(new GitHistory(rev));
            }
            Collections.reverse(gitHistory);
        } catch (RepositoryNotFoundException ex) {
            git = Git.init().setDirectory(repoPath).call();
        } catch (NoHeadException ex) {
            git = Git.init().setDirectory(repoPath).call();
        }

        Db.exec((api) -> {
            for (RecArticle a : api.getArticleMapper().listArticles(null, new ArticlesFilter())) {
                headers.put(a.getArticleId(), a.getHeader());
                articleTypes.put(a.getArticleId(), a.getArticleType());
            }
            history = api.getArticleHistoryMapper().retrieveHistoryHeadersForExport();
        });

        Collections.sort(history, new Comparator<RecArticleHistory>() {
            @Override
            public int compare(RecArticleHistory o1, RecArticleHistory o2) {
                return Long.compare(o2.getHistoryId(), o1.getHistoryId());
            }
        });

        for (RecArticleHistory h : history) {
            if (h.getOldHeader() != null) {
                headers.put(h.getArticleId(), h.getOldHeader());
            }
        }

        Collections.sort(history, new Comparator<RecArticleHistory>() {
            @Override
            public int compare(RecArticleHistory o1, RecArticleHistory o2) {
                return Long.compare(o1.getHistoryId(), o2.getHistoryId());
            }
        });

        int count = 0;
        for (RecArticleHistory h : history) {
            String oldHeader = StringUtils.equals(h.getOldHeader(), h.getNewHeader()) ? null : h.getOldHeader();
            if (h.getNewHeader() != null) {
                headers.put(h.getArticleId(), h.getNewHeader());
            }
            count++;
            System.out.println(
                    "Export " + count + "/" + history.size() + ": #" + h.getHistoryId() + " from " + h.getChanged());
            add(h, oldHeader);
            if (count % 1000 == 0) {
                git.gc().call();
            }
        }
        git.close();
    }

    static void add(RecArticleHistory h, String oldHeader) {
        try {
            String header = headers.get(h.getArticleId());
            if (header == null) {
                header = "change";
            }
            // and then commit the changes
            PersonIdent pi = new PersonIdent(h.getChanger(), "user@localhost", h.getChanged(), TimeZone.getDefault());
            String message = "#" + h.getHistoryId() + ": " + header;

            if (!gitHistory.isEmpty()) {
                GitHistory h0 = gitHistory.remove(0);
                GitHistory c = new GitHistory(pi, message);
                if (h0.equals(c)) {
                    return;
                } else {
                    System.err.println("There is wrong history record: " + message);
                    System.exit(1);
                }
            }

            Db.exec((api) -> {
                fullCurrentHistory = api.getArticleHistoryMapper().getHistory(h.getHistoryId());
            });

            String articleType = articleTypes.get(h.getArticleId());
            // check if already exist in git
            if (oldHeader != null) {
                String oldFile = articleType + '/' + oldHeader + '-' + h.getArticleId() + ".xml";
                new File(repoPath, oldFile).delete();
                git.rm().addFilepattern(oldFile).call();
            }
            String newFile = articleType + '/' + header.replace("<", "").replace(">", "") + '-' + h.getArticleId()
                    + ".xml";
            String xml = xml2text(fullCurrentHistory.getNewXml());
            FileUtils.writeStringToFile(new File(repoPath, newFile), xml, StandardCharsets.UTF_8);
            git.add().addFilepattern(newFile).call();

            CommitCommand cc = git.commit();
            cc.setCommitter(pi).setMessage(message).call();
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

    static class GitHistory {
        String changer;
        long changed;
        String message;

        public GitHistory(PersonIdent pi, String message) {
            changer = pi.getName();
            changed = pi.getWhen().getTime() / 1000;
            this.message = message;
        }

        public GitHistory(RevCommit rev) {
            this(rev.getCommitterIdent(), rev.getShortMessage());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GitHistory) {
                GitHistory o = (GitHistory) obj;
                return changed == o.changed && changer.equals(o.changer) && message.equals(o.message);
            } else {
                return false;
            }
        }
    }
}
