package org.im.dc.server.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;

import org.im.dc.server.Db;
import org.junit.Before;
import org.junit.Test;

public class DbTestArticle {

    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
    }

    @Test
    public void testArticle() {
        RecArticle recSource = new RecArticle();
        recSource.setAssignedUsers(new String[] { "C", "D" });
        recSource.setLettersCount(3);
        recSource.setLinkedTo(new String[] { "I", "J" });
        recSource.setMarkers(new String[] { "E", "F" });
        recSource.setNotes("notes1");
        recSource.setState("initial");
        recSource.setTextForSearch("text1");
        recSource.setWatchers(new String[] { "G", "H" });
        recSource.setWords(new String[] { "A", "B" });
        recSource.setXml(new byte[] { 2, 3, 4 });
        recSource.setLastUpdated(new Date(3344));

        Db.exec((api) -> api.getArticleMapper().insertArticle(recSource));
        check1(recSource);

        final RecArticle rec = Db
                .execAndReturn((api) -> api.getArticleMapper().selectArticle(recSource.getArticleId()));
        check1(rec);

        rec.setAssignedUsers(new String[] { "Z" });
        rec.setLettersCount(2);
        rec.setLinkedTo(new String[] { "X" });
        rec.setMarkers(new String[] { "Y" });
        rec.setNotes("notes2");
        rec.setState("next");
        rec.setTextForSearch("text2");
        rec.setWatchers(new String[] { "W" });
        rec.setWords(new String[] { "U" });
        rec.setXml(new byte[] { 5, 6 });
        rec.setLastUpdated(new Date(5566));

        int uwrong = Db.execAndReturn((api) -> api.getArticleMapper().updateArticle(rec, new Date(1)));
        assertEquals(0, uwrong);

        int u = Db.execAndReturn((api) -> api.getArticleMapper().updateArticle(rec, new Date(3344)));
        assertEquals(1, u);
        check2(rec);
        RecArticle rec2 = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(recSource.getArticleId()));
        check2(rec2);
    }

    @Test
    public void testList() {
        Db.exec((api) -> api.getArticleMapper().list(null));
        Db.exec((api) -> api.getArticleMapper().list("state"));
    }

    void check1(RecArticle rec) {
        assertTrue(rec.getArticleId() > 0);
        assertArrayEquals(new String[] { "C", "D" }, rec.getAssignedUsers());
        assertEquals(3, rec.getLettersCount());
        assertArrayEquals(new String[] { "I", "J" }, rec.getLinkedTo());
        assertArrayEquals(new String[] { "E", "F" }, rec.getMarkers());
        assertEquals("notes1", rec.getNotes());
        assertEquals("initial", rec.getState());
        assertEquals("text1", rec.getTextForSearch());
        assertArrayEquals(new String[] { "G", "H" }, rec.getWatchers());
        assertArrayEquals(new String[] { "A", "B" }, rec.getWords());
        assertArrayEquals(new byte[] { 2, 3, 4 }, rec.getXml());
        assertEquals(new Date(3344), rec.getLastUpdated());
    }

    void check2(RecArticle rec) {
        assertTrue(rec.getArticleId() > 0);
        assertArrayEquals(new String[] { "Z" }, rec.getAssignedUsers());
        assertEquals(2, rec.getLettersCount());
        assertArrayEquals(new String[] { "X" }, rec.getLinkedTo());
        assertArrayEquals(new String[] { "Y" }, rec.getMarkers());
        assertEquals("notes2", rec.getNotes());
        assertEquals("next", rec.getState());
        assertEquals("text2", rec.getTextForSearch());
        assertArrayEquals(new String[] { "W" }, rec.getWatchers());
        assertArrayEquals(new String[] { "U" }, rec.getWords());
        assertArrayEquals(new byte[] { 5, 6 }, rec.getXml());
        assertEquals(new Date(5566), rec.getLastUpdated());
    }
}
