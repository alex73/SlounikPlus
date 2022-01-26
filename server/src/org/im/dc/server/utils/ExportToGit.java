package org.im.dc.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.db.RecArticleHistory;

public class ExportToGit {
    static final Collator BE = Collator.getInstance(new Locale("be"));

    static Path repoPath;
    static Git git;
    static List<RecArticleHistory> history;
    static List<GitHistory> gitHistory = new ArrayList<>();
    static Map<Integer, ArticleInfo> articleInfos = new HashMap<>();
    static Map<String, Map<Integer, Set<String>>> assignments = new TreeMap<>(); // <type, <id, <user>>>

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ExportToGit <git_dir>");
            System.exit(1);
        }

        Db.init(System.getProperty("CONFIG_DIR"));

        repoPath = Paths.get(args[0]);

        try {
            git = Git.open(repoPath.toFile());
            Iterable<RevCommit> log = git.log().call();
            for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
                RevCommit rev = iterator.next();
                gitHistory.add(new GitHistory(rev));
            }
            Collections.reverse(gitHistory);
        } catch (RepositoryNotFoundException ex) {
            git = Git.init().setDirectory(repoPath.toFile()).call();
        } catch (NoHeadException ex) {
            git = Git.init().setDirectory(repoPath.toFile()).call();
        }

        // list history
        Db.exec((api) -> {
            for (RecArticle a : api.getArticleMapper().getAllArticles(null)) {
                articleInfos.put(a.getArticleId(), new ArticleInfo(a));
            }
            history = api.getArticleHistoryMapper().retrieveHistoryHeadersForExport();
        });

        // calculate initial state
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
                ai.setAssignedUsers(h.getOldAssignedUsers());
            }
        }

        // history export
        for (RecArticleHistory h : history) {
            ArticleInfo ai = articleInfos.get(h.getArticleId());
            if (h.getNewHeader() != null) {
                ai.header = h.getNewHeader();
            }
            if (h.getNewState() != null) {
                ai.state = h.getNewState();
            }
            if (h.getNewAssignedUsers() != null) {
                ai.setAssignedUsers(h.getNewAssignedUsers());
            }
            Path newFile = ai.getPath();
            applyAssignments(ai);

            System.out.println("Export " + count + "/" + history.size() + ": #" + h.getHistoryId() + " from " + h.getChanged());
            add(h, ai, newFile);
        }

        // export current snapshot and assignments
        Set<Path> added = new HashSet<>();
        added.addAll(setCurrentArticles());
        added.addAll(setCurrentAssignments());

        Set<String> forDelete = new HashSet<>();
        Files.find(repoPath, Integer.MAX_VALUE, (p, a) -> a.isRegularFile()).filter(p -> !added.contains(p)).map(p -> repoPath.relativize(p).toString())
                .filter(n -> !n.startsWith(".git/") && !n.startsWith("_db/")).forEach(n -> forDelete.add(n));

        if (!added.isEmpty()) {
            AddCommand add = git.add();
            added.forEach(p -> add.addFilepattern(repoPath.relativize(p).toString()));
            add.call();
        }
        if (!forDelete.isEmpty()) {
            RmCommand rm = git.rm();
            for (String f : forDelete) {
                Files.delete(repoPath.resolve(f));
                rm.addFilepattern(f);
            }
            rm.call();
        }
        try {
            CommitCommand cc = git.commit();
            cc.setAllowEmpty(false).setCommitter(new PersonIdent("admin", "db@localhost", new Date(), TimeZone.getDefault()))
                    .setMessage("Snapshot of current db state").call();
        } catch (EmptyCommitException ex) {
        }

        git.close();
    }

    // запісвае бягучы стан, без гісторыі
    static Set<Path> setCurrentArticles() throws Exception {
        Set<Path> added = new HashSet<>();
        int pos = 0;
        for (int id : articleInfos.keySet().toArray(new Integer[0])) {
            pos++;
            System.out.print("Check for change " + pos + "/" + articleInfos.size() + ": #" + id);
            RecArticle a = Db.execAndReturn((api) -> {
                return api.getArticleMapper().selectArticle(id);
            });
            ArticleInfo ai = new ArticleInfo(a);
            articleInfos.put(id, ai);
            applyAssignments(ai);
            Path newFile = ai.getPath();
            byte[] xml = xmlFormat(a.getXml());
            byte[] existXml = Files.exists(newFile) ? Files.readAllBytes(newFile) : new byte[0];
            if (!Arrays.equals(xml, existXml)) {
                Files.createDirectories(newFile.getParent());
                Files.write(newFile, xml);
                System.out.println(" - changed " + newFile);
            } else {
                System.out.println();
            }
            added.add(newFile);
        }
        return added;
    }

    static Set<Path> setCurrentAssignments() throws Exception {
        Set<Path> added = new HashSet<>();
        for (String type : articleInfos.values().stream().map(ai -> ai.type).sorted().distinct().collect(Collectors.toList())) {
            added.add(saveAssignments(type));
        }
        return added;
    }

    static int count, added;

    static void add(RecArticleHistory h, ArticleInfo ai, Path newFile) {
        count++;
        try {
            // and then commit the changes
            PersonIdent pi = new PersonIdent(h.getChanger(), "user@localhost", h.getChanged(), TimeZone.getDefault());
            String message = "#" + h.getHistoryId() + " " + repoPath.relativize(newFile);

            while (!gitHistory.isEmpty()) {
                GitHistory h0 = gitHistory.remove(0);
                if (!h0.message.startsWith("#")) {
                    continue;
                }
                GitHistory c = new GitHistory(pi, message);
                if (h0.changed == c.changed && h0.changer.equals(c.changer) && h0.message.replaceAll(" .+", "").equals(c.message.replaceAll(" .+", ""))) {
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

            for (Path oldFile : getOldFilePaths(h.getArticleId())) {
                if (!newFile.equals(oldFile)) {
                    Files.delete(oldFile);
                    git.rm().addFilepattern(repoPath.relativize(oldFile).toString()).call();
                }
            }
            if (fullCurrentHistory.getNewXml() != null) {
                byte[] xml = xmlFormat(fullCurrentHistory.getNewXml());
                Files.createDirectories(newFile.getParent());
                Files.write(newFile, xml);
            }
            saveAssignments(ai.type);
            git.add().setUpdate(false).addFilepattern(repoPath.relativize(newFile).toString()).call();

            CommitCommand cc = git.commit();
            cc.setCommitter(pi).setMessage(message).call();

            if (added % 500 == 0) {
                // git.gc().call();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static void applyAssignments(ArticleInfo ai) {
        if (ai.assignedUsers != null) {
            Map<Integer, Set<String>> t = assignments.computeIfAbsent(ai.type, k -> new TreeMap<>());
            t.put(ai.articleId, ai.assignedUsers);
        }
    }

    static Path saveAssignments(String type) throws Exception {
        Map<Integer, Set<String>> t = assignments.computeIfAbsent(type, k -> new HashMap<>());
        List<String> lines = t.entrySet().stream().map(en -> en.getKey() + ": " + en.getValue()).collect(Collectors.toList());
        String fn = type + "-assignments.txt";
        Path r = repoPath.resolve(fn);
        Files.write(r, lines);
        git.add().setUpdate(false).addFilepattern(fn).call();
        return r;
    }

    static List<Path> getOldFilePaths(int articleId) throws IOException {
        return Files.find(repoPath, Integer.MAX_VALUE, (p, a) -> a.isRegularFile() && p.toString().endsWith("-" + articleId + ".xml"))
                .collect(Collectors.toList());
    }

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private static byte[] xmlFormat(byte[] xml) throws Exception {
        if (xml == null) {
            return new byte[0];
        }
        Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult res = new StreamResult(out);
        Source sou = new StreamSource(new ByteArrayInputStream(xml));
        transformer.transform(sou, res);
        return out.toByteArray();
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
        public final int articleId;
        public final String type;
        public String header;
        public String state;
        public Set<String> assignedUsers;

        public ArticleInfo(RecArticle a) {
            articleId = a.getArticleId();
            type = a.getArticleType();
            header = a.getHeader();
            state = a.getState();
            setAssignedUsers(a.getAssignedUsers());
        }

        public void setAssignedUsers(String[] users) {
            assignedUsers = new TreeSet<>(BE);
            assignedUsers.addAll(users != null ? Arrays.asList(users) : Collections.emptyList());
        }

        public Path getPath() {
            return repoPath.resolve((type + '/' + state + '/' + header.replace("<", "").replace(">", "").replace("/", "_") + '-' + articleId + ".xml")
                    .replaceAll("/{2,}", "/"));
        }
    }
}
