package org.im.dc.server.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.im.dc.server.Db;
import org.im.dc.service.dto.ArticlesFilter;
import org.junit.Before;
import org.junit.Test;

public class DbTestArticle {

    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init(".");
    }

    @Test
    public void testInsertMany() throws Exception {
        List<RecArticle> list = new ArrayList<>();
        RecArticle r1 = new RecArticle();
        r1.setAssignedUsers(new String[] { "u1", "u2" });
        r1.setHeader("word1");
        r1.setState("Неапрацаванае");
        r1.setMarkers(new String[0]);
        r1.setWatchers(new String[0]);
        r1.setLinkedTo(new String[0]);
        r1.setLastUpdated(new Date());
        list.add(r1);
        RecArticle r2 = new RecArticle();
        r2.setAssignedUsers(new String[] { "u1", "u2" });
        r2.setHeader("word2");
        r2.setState("Неапрацаванае");
        r2.setMarkers(new String[0]);
        r2.setWatchers(new String[0]);
        r2.setLinkedTo(new String[0]);
        r2.setLastUpdated(new Date());
        list.add(r2);
        Db.exec((api) -> api.getArticleMapper().insertArticles(list));
    }

    @Test
    public void testArticle() throws Exception {
        RecArticle recSource = new RecArticle();
        recSource.setAssignedUsers(new String[] { "C", "D" });
        recSource.setLettersCount(3);
        recSource.setLinkedTo(new String[] { "I", "J" });
        recSource.setMarkers(new String[] { "E", "F" });
        recSource.setState("initial");
        recSource.setTextForSearch("text1");
        recSource.setWatchers(new String[] { "G", "H" });
        recSource.setHeader("A, B");
        recSource.setXml(new byte[] { 2, 3, 4 });
        recSource.setLastUpdated(new Date(3344));

        Db.exec((api) -> api.getArticleMapper().insertArticle(recSource));
        check1(recSource);

        final RecArticle rec = Db
                .execAndReturn((api) -> api.getArticleMapper().selectArticleForUpdate(recSource.getArticleId()));
        check1(rec);

        rec.setAssignedUsers(new String[] { "Z" });
        rec.setLettersCount(2);
        rec.setLinkedTo(new String[] { "X" });
        rec.setMarkers(new String[] { "Y" });
        rec.setState("next");
        rec.setTextForSearch("text2");
        rec.setWatchers(new String[] { "W" });
        rec.setHeader("U");
        rec.setXml(new byte[] { 5, 6 });
        rec.setLastUpdated(new Date(5566));

        int uwrong = Db.execAndReturn((api) -> api.getArticleMapper().updateArticle(rec, new Date(1)));
        assertEquals(0, uwrong);

        int u = Db.execAndReturn((api) -> api.getArticleMapper().updateArticle(rec, new Date(3344)));
        assertEquals(1, u);
        check2(rec);
        RecArticle rec2 = Db
                .execAndReturn((api) -> api.getArticleMapper().selectArticleForUpdate(recSource.getArticleId()));
        check2(rec2);
    }

    @Test
    public void testList() throws Exception {
        ArticlesFilter filter = new ArticlesFilter();
        List<RecArticle> list = Db.execAndReturn((api) -> api.getArticleMapper().listArticles(null, filter));
        filter.states = Arrays.asList("state");
        Db.exec((api) -> api.getArticleMapper().listArticles(null, filter));
    }

    @Test
    public void testLinkedTo() throws Exception {
        Db.execAndReturn((api) -> api.getArticleMapper().selectLinkedTo("A, L"));
    }

    void check1(RecArticle rec) {
        assertTrue(rec.getArticleId() > 0);
        assertArrayEquals(new String[] { "C", "D" }, rec.getAssignedUsers());
        assertEquals(3, rec.getLettersCount());
        assertArrayEquals(new String[] { "I", "J" }, rec.getLinkedTo());
        assertArrayEquals(new String[] { "E", "F" }, rec.getMarkers());
        assertEquals("initial", rec.getState());
        assertEquals("text1", rec.getTextForSearch());
        assertArrayEquals(new String[] { "G", "H" }, rec.getWatchers());
        assertEquals("A, B", rec.getHeader());
        assertArrayEquals(new byte[] { 2, 3, 4 }, rec.getXml());
        assertEquals(new Date(3344), rec.getLastUpdated());
    }

    void check2(RecArticle rec) {
        assertTrue(rec.getArticleId() > 0);
        assertArrayEquals(new String[] { "Z" }, rec.getAssignedUsers());
        assertEquals(2, rec.getLettersCount());
        assertArrayEquals(new String[] { "X" }, rec.getLinkedTo());
        assertArrayEquals(new String[] { "Y" }, rec.getMarkers());
        assertEquals("next", rec.getState());
        assertEquals("text2", rec.getTextForSearch());
        assertArrayEquals(new String[] { "W" }, rec.getWatchers());
        assertEquals("U", rec.getHeader());
        assertArrayEquals(new byte[] { 5, 6 }, rec.getXml());
        assertEquals(new Date(5566), rec.getLastUpdated());
    }
}
