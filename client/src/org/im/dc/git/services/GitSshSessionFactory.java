package org.im.dc.git.services;

import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Properties;

import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschSession;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GitSshSessionFactory extends SshSessionFactory {
    private final JSch defaultJSch;
    private final Properties settings;

    public GitSshSessionFactory() throws Exception {
        defaultJSch = new JSch();

        settings = new Properties();
        try (FileInputStream in = new FileInputStream("settings.properties")) {
            settings.load(in);
        }
        if (settings.getProperty("fingerprint-host") == null) {
            throw new Exception("'fingerprint-host' should be defined in settings.properties");
        }
        if (settings.getProperty("fingerprint") == null) {
            throw new Exception("'fingerprint' should be defined in settings.properties");
        }
        if (settings.getProperty("key-type") == null) {
            throw new Exception("'key-type' should be defined in settings.properties");
        }
    }

    @Override
    public RemoteSession getSession(URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms)
            throws TransportException {
        String user = uri.getUser();
        final String pass = uri.getPass();
        String host = uri.getHost();
        int port = uri.getPort();
        try {
            defaultJSch.addIdentity("main", Files.readAllBytes(Paths.get("key")), null, null);

            Session session = createSession(credentialsProvider, fs, user, pass, host, port);
            session.connect(tms);

            return new JschSession(session, uri);
        } catch (JSchException je) {
            final Throwable c = je.getCause();
            if (c instanceof UnknownHostException) {
                throw new TransportException(uri, JGitText.get().unknownHost, je);
            }
            if (c instanceof ConnectException) {
                throw new TransportException(uri, c.getMessage(), je);
            }
            throw new TransportException(uri, je.getMessage(), je);
        } catch (Exception ex) {
            throw new TransportException(uri, ex.getMessage(), ex);
        }
    }

    private Session createSession(CredentialsProvider credentialsProvider, FS fs, String user, final String pass,
            String host, int port) throws Exception {
        final Session session = createSession(user, host, port, fs);

        // session.setPassword(pass);
        session.setConfig("StrictHostKeyChecking", "yes");
        session.setConfig("PreferredAuthentications", "publickey");
        session.setHostKeyRepository(known_hosts);

        session.setUserInfo(userInfo);

        return session;
    }

    protected Session createSession(final String user, final String host, final int port, FS fs) throws JSchException {
        return defaultJSch.getSession(user, host, port);
    }

    HostKeyRepository known_hosts = new HostKeyRepository() {
        @Override
        public int check(String host, byte[] key) {
            if (settings.getProperty("fingerprint-host").equals(host)) {
                if (compareFingerprints(settings.getProperty("fingerprint"), key)) {
                    return HostKeyRepository.OK;
                } else {
                    return HostKeyRepository.CHANGED;
                }
            } else {
                return HostKeyRepository.NOT_INCLUDED;
            }
        }

        private boolean compareFingerprints(String expectedFingerprint, byte[] key) {
            try {
                MessageDigest hash = MessageDigest.getInstance("MD5");
                byte[] digest = hash.digest(key);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    int bar = digest[i] & 0xff;
                    if (sb.length() > 0) {
                        sb.append(':');
                    }
                    sb.append(chars[(bar >>> 4) & 0xf]);
                    sb.append(chars[(bar) & 0xf]);
                }
                return expectedFingerprint.equals(sb.toString());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void add(HostKey hostkey, UserInfo ui) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void remove(String host, String type) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void remove(String host, String type, byte[] key) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public String getKnownHostsRepositoryID() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public HostKey[] getHostKey() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public HostKey[] getHostKey(String host, String type) {
            if (!settings.getProperty("fingerprint-host").equals(host)) {
                throw new RuntimeException("Unknown host");
            }
            if (!settings.getProperty("key-type").equals(type)) {
                throw new RuntimeException("Unknown key type");
            }
            return new HostKey[0];
        }
    };

    UserInfo userInfo = new UserInfo() {
        @Override
        public String getPassphrase() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public String getPassword() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public boolean promptPassword(String message) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public boolean promptPassphrase(String message) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public boolean promptYesNo(String message) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void showMessage(String message) {
            throw new RuntimeException("Not implemented");
        }
    };

    private static String[] chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

}
