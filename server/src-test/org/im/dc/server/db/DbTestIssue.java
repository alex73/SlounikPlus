package org.im.dc.server.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;

import org.im.dc.server.Db;
import org.junit.Before;
import org.junit.Test;

public class DbTestIssue {
    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
    }

    @Test
    public void testInsert() throws Exception {
        RecIssue rec1 = new RecIssue();
        rec1.setArticleId(1);
        rec1.setCreated(new Date(123));
        rec1.setAuthor("ch1234");
        rec1.setComment("comm11");
        rec1.setOldXml(new byte[] { 1, 2 });
        rec1.setNewXml(new byte[] { 3, 4 });
        rec1.setFixed(new Date(22));
        rec1.setFixer("f1");
        rec1.setAccepted(true);

        Db.exec((api) -> api.getIssueMapper().insertIssue(rec1));
        check(rec1);
    }

    @Test
    public void testRetrieveUserIssues() throws Exception {
        Db.exec((api) -> api.getSession().selectList("retrieveUserIssues", "user"));
    }

    void check(RecIssue rec) {
        assertTrue(rec.getIssueId() > 0);
        assertEquals(1, rec.getArticleId());
        assertEquals(new Date(123), rec.getCreated());
        assertEquals("ch1234", rec.getAuthor());
        assertEquals("comm11", rec.getComment());
        assertArrayEquals(new byte[] { 1, 2 }, rec.getOldXml());
        assertArrayEquals(new byte[] { 3, 4 }, rec.getNewXml());
        assertEquals(new Date(22), rec.getFixed());
        assertEquals("f1", rec.getFixer());
        assertEquals(true, rec.isAccepted());
    }
}
