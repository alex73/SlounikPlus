package org.im.dc.server.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
    static Map<Integer, ArticleInfo> articleInfos = new HashMap<>();

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

        // list history
        Db.exec((api) -> {
            for (RecArticle a : api.getArticleMapper().listArticles(null, new ArticlesFilter())) {
                articleInfos.put(a.getArticleId(), new ArticleInfo(a));
            }
            history = api.getArticleHistoryMapper().retrieveHistoryHeadersForExport();
        });

        for (ListIterator<RecArticleHistory> it = history.listIterator(history.size()); it.hasPrevious();) {
            RecArticleHistory h = it.previous();
            ArticleInfo ai = articleInfos.get(h.getArticleId());
            if (h.getOldHeader() != null) {
                ai.header = h.getOldHeader();
            }
            if (h.getOldState() != null) {
                ai.state = h.getOldState();
            }
            if (h.getOldAssignedUsers() != null) {
                ai.assignedUsers = h.getOldAssignedUsers();
            }
        }

        for (RecArticleHistory h : history) {
            ArticleInfo ai = articleInfos.get(h.getArticleId());
            String oldFile = ai.getPath(h.getArticleId());
            if (h.getNewHeader() != null) {
                ai.header = h.getNewHeader();
            }
            if (h.getNewState() != null) {
                ai.state = h.getNewState();
            }
            if (h.getNewAssignedUsers() != null) {
                ai.assignedUsers = h.getNewAssignedUsers();
            }
            String newFile = ai.getPath(h.getArticleId());

            System.out.println(
                    "Export " + count + "/" + history.size() + ": #" + h.getHistoryId() + " from " + h.getChanged());
            add(h, ai, oldFile, newFile);
        }
        git.close();
    }

    static int count, added;

    static void add(RecArticleHistory h, ArticleInfo ai, String oldFile, String newFile) {
        count++;
        try {
            // and then commit the changes
            PersonIdent pi = new PersonIdent(h.getChanger(), "user@localhost", h.getChanged(), TimeZone.getDefault());
            String message = "#" + h.getHistoryId() + " " + newFile;

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
            added++;

            RecArticleHistory fullCurrentHistory = Db.execAndReturn((api) -> {
                return api.getArticleHistoryMapper().getHistory(h.getHistoryId());
            });

            if (oldFile.equals(newFile)) {
                if (fullCurrentHistory.getNewXml() != null) {
                    String xml = xml2text(fullCurrentHistory.getNewXml());
                    FileUtils.writeStringToFile(new File(repoPath, newFile), xml, StandardCharsets.UTF_8);
                    git.add().addFilepattern(newFile).call();
                }
            } else {
                if (new File(repoPath, oldFile).exists()) {
                    new File(repoPath, oldFile).renameTo(new File(repoPath, newFile));
                }
                new File(repoPath, oldFile).delete();
                git.rm().addFilepattern(oldFile).call();
                if (fullCurrentHistory.getNewXml() != null) {
                    String xml = xml2text(fullCurrentHistory.getNewXml());
                    FileUtils.writeStringToFile(new File(repoPath, newFile), xml, StandardCharsets.UTF_8);
                }
                git.add().addFilepattern(newFile).call();
            }

            CommitCommand cc = git.commit();
            cc.setCommitter(pi).setMessage(message).call();

            if (added % 500 == 0) {
                git.gc().call();
            }
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

    static class ArticleInfo {
        public String type;
        public String header;
        public String state;
        public String[] assignedUsers;

        public ArticleInfo(RecArticle a) {
            type = a.getArticleType();
            header = a.getHeader();
            state = a.getState();
            assignedUsers = a.getAssignedUsers();
        }

        public String getPath(int articleId) {
            return type + '/' + header.replace("<", "").replace(">", "") + '-' + state + '-'
                    + Arrays.toString(assignedUsers) + '-' + articleId + ".xml";
        }
    }
}
