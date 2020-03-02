package org.im.dc.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.im.dc.server.db.DoArticle;
import org.im.dc.server.db.DoArticleHistory;
import org.im.dc.server.db.DoArticleNote;
import org.im.dc.server.db.DoComment;
import org.im.dc.server.db.DoIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry for DB access.
 */
public class Db {
    private static final Logger LOG = LoggerFactory.getLogger(Db.class);

    private static SqlSessionFactory sqlSessionFactory;

    public static void init(String configDir) throws Exception {
        File config = new File(configDir, "db.properties");
        LOG.info("Loading db info from {}", config.getAbsolutePath());
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(config)) {
            props.load(in);
        }

        String resource = "/org/im/dc/server/db/mybatis-config.xml";

        try (InputStream inputStream = Db.class.getResourceAsStream(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);
        }
    }

    /**
     * Execute DB operations inside transaction.
     */
    public static <T> T execAndReturn(DbExecutorResult<T> exec) throws Exception {
        T result;
        LOG.debug("Start transaction");
        try (SqlSession s = sqlSessionFactory.openSession(ExecutorType.REUSE,
                TransactionIsolationLevel.READ_COMMITTED)) {
            try {
                result = exec.run(new Api(s));
                s.commit();
                LOG.debug("Commit transaction");
            } catch (Throwable ex) {
                LOG.warn("Error in transaction", ex);
                s.rollback();
                throw ex;
            }
        }
        return result;
    }

    public static void exec(DbExecutor exec) throws Exception {
        LOG.debug("Start transaction");
        try (SqlSession s = sqlSessionFactory.openSession(ExecutorType.REUSE,
                TransactionIsolationLevel.READ_COMMITTED)) {
            try {
                exec.run(new Api(s));
                s.commit();
                LOG.debug("Commit transaction");
            } catch (Throwable ex) {
                LOG.warn("Error in transaction", ex);
                s.rollback();
                throw ex;
            }
        }
    }

    public static void execBatch(DbExecutor exec) throws Exception {
        LOG.debug("Start batch transaction");
        try (SqlSession s = sqlSessionFactory.openSession(ExecutorType.BATCH,
                TransactionIsolationLevel.READ_COMMITTED)) {
            try {
                exec.run(new Api(s));
                s.commit();
                LOG.debug("Commit batch transaction");
            } catch (Throwable ex) {
                LOG.warn("Error in batch transaction", ex);
                s.rollback();
                throw ex;
            }
        }
    }

    public static class Api {
        private final SqlSession s;

        public Api(SqlSession s) {
            this.s = s;
        }

        public SqlSession getSession() {
            return s;
        }

        public DoArticle getArticleMapper() {
            return s.getMapper(DoArticle.class);
        }

        public DoArticleNote getArticleNoteMapper() {
            return s.getMapper(DoArticleNote.class);
        }

        public DoArticleHistory getArticleHistoryMapper() {
            return s.getMapper(DoArticleHistory.class);
        }

        public DoComment getCommentMapper() {
            return s.getMapper(DoComment.class);
        }

        public DoIssue getIssueMapper() {
            return s.getMapper(DoIssue.class);
        }
    }
}
