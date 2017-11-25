package org.im.dc.server.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;

import org.im.dc.server.Db;
import org.junit.Before;
import org.junit.Test;

public class DbTestArticleHistory {
    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
    }

    @Test
    public void testInsert() throws Exception {
        RecArticleHistory rec1 = new RecArticleHistory();
        rec1.setArticleId(1);
        rec1.setChanged(new Date(123));
        rec1.setChanger("ch123");

        rec1.setOldAssignedUsers(new String[] { "u1", "u2" });
        rec1.setNewAssignedUsers(new String[] { "u3", "u4" });
        rec1.setOldState("st1");
        rec1.setNewState("st2");
        rec1.setOldXml(new byte[] { 1, 2 });
        rec1.setNewXml(new byte[] { 3, 4 });

        Db.exec((api) -> api.getArticleHistoryMapper().insertArticleHistory(rec1));
        check(rec1);
    }

    void check(RecArticleHistory rec) {
        assertTrue(rec.getHistoryId() > 0);
        assertEquals(1, rec.getArticleId());
        assertEquals(new Date(123), rec.getChanged());

        assertArrayEquals(new String[] { "u1", "u2" }, rec.getOldAssignedUsers());
        assertArrayEquals(new String[] { "u3", "u4" }, rec.getNewAssignedUsers());
        assertEquals("st1", rec.getOldState());
        assertEquals("st2", rec.getNewState());

        assertArrayEquals(new byte[] { 1, 2 }, rec.getOldXml());
        assertArrayEquals(new byte[] { 3, 4 }, rec.getNewXml());
    }
}
