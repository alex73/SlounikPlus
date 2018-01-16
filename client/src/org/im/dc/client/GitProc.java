package org.im.dc.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.im.dc.git.services.GitSshSessionFactory;

public class GitProc {
    public static final String REPO_DIR = "repo";
    private final Path localDir;
    private final Repository localRepo;

    protected static GitProc INSTANCE;

    public static Map<String, Schema> configSchemas;

    public static GitProc getInstance() {
        return INSTANCE;
    }

    public GitProc(String repoPath, String uri) throws Exception {
        SshSessionFactory.setInstance(new GitSshSessionFactory());

        localDir = Paths.get(repoPath);
        boolean needReset;
        if (!Files.isDirectory(localDir)) {
            // clone
            CloneCommand clone = Git.cloneRepository();
            clone.setDirectory(localDir.toFile());
            clone.setURI(uri);
            clone.call();
            needReset = false;
        } else {
            needReset = true;
        }

        localRepo = new FileRepositoryBuilder().setWorkTree(localDir.toFile()).readEnvironment().findGitDir().build();

        if (needReset) {
            reset();
        }
    }

    public synchronized void reset() throws GitAPIException {
        try (Git git = new Git(localRepo)) {
            git.reset().setMode(ResetType.HARD).call();
            git.clean().setForce(true).setCleanDirectories(true).call();
            git.fetch().call();
            git.checkout().setName("origin/master").call();
            git.branchDelete().setBranchNames("master").call();
            git.checkout().setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM).setCreateBranch(true).setName("master")
                    .call();
        }
    }

    public synchronized void add(Path file) throws GitAPIException {
        try (Git git = new Git(localRepo)) {
            git.add().addFilepattern(localDir.relativize(file).toString()).call();
        }
    }

    public synchronized void remove(Path file) throws GitAPIException {
        try (Git git = new Git(localRepo)) {
            git.rm().addFilepattern(localDir.relativize(file).toString()).call();
        }
    }

    public synchronized void commit(String user, String email) throws GitAPIException {
        try (Git git = new Git(localRepo)) {
            git.commit().setAuthor(new PersonIdent(user, email)).setMessage("changes").call();
            git.push().call();
        }
    }

    public Path getLocalDir() {
        return localDir;
    }

    Map<String, Integer> articleIds = new HashMap<>();
    int latestId;

    public synchronized int getId(Path p) {
        Integer have = articleIds.get(localDir.relativize(p).toString());
        if (have == null) {
            have = ++latestId;
            articleIds.put(localDir.relativize(p).toString(), have);
        }
        return have;
    }

    public synchronized Path getPathById(int id) {
        for (Map.Entry<String, Integer> en : articleIds.entrySet()) {
            if (id == en.getValue().intValue()) {
                return localDir.resolve(en.getKey());
            }
        }
        throw new RuntimeException("Article by ID not found");
    }
}
