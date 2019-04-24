package org.im.dc.server.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;

import org.im.dc.server.Db;
import org.junit.Before;
import org.junit.Test;

public class DbTestComment {
    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init(".");
    }

    @Test
    public void testInsert() throws Exception {
        RecComment rec1 = new RecComment();
        rec1.setArticleId(1);
        rec1.setCreated(new Date(123));
        rec1.setAuthor("ch123");
        rec1.setComment("comm1");

        Db.exec((api) -> api.getCommentMapper().insertComment(rec1));
        check(rec1);
    }

    void check(RecComment rec) {
        assertTrue(rec.getCommentId() > 0);
        assertEquals(1, rec.getArticleId());
        assertEquals(new Date(123), rec.getCreated());
        assertEquals("ch123", rec.getAuthor());
        assertEquals("comm1", rec.getComment());
    }
}
