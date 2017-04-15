package org.im.dc.server.startup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;

public class RemoveArticles {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ImportArticles <dir>");
            System.exit(1);
        }

        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
        Config.load();
        File[] ls = new File(args[0]).listFiles();
        if (ls == null) {
            return;
        }

        Date lastUpdated = new Date();
        List<RecArticle> articles = new ArrayList<>();
        for (File f : ls) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                String[] words = f.getName().replaceAll("\\.xml$", "").split(",");
                byte[] xml = Files.readAllBytes(f.toPath());

                try {
                    Validator validator = Config.articleSchema.newValidator();
                    validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
                } catch (Exception ex) {
                    System.err.println("Error xml validation from " + f);
                    ex.printStackTrace();
                }

                RecArticle a = new RecArticle();
                a.setWords(words);
                a.setXml(xml);
                a.setAssignedUsers(new String[0]);
                a.setState("Неапрацаванае");
                a.setMarkers(new String[0]);
                a.setWatchers(new String[0]);
                a.setLinkedTo(new String[0]);
                a.setLastUpdated(lastUpdated);
                articles.add(a);
            }
        }

        Db.exec((api) -> {
            try {
                api.getSession().getConnection().createStatement().execute("DELETE FROM ArticleNotes");
                api.getSession().getConnection().createStatement().execute("DELETE FROM Issues");
                api.getSession().getConnection().createStatement().execute("DELETE FROM Comments");
                api.getSession().getConnection().createStatement().execute("DELETE FROM ArticlesHistory");
                api.getSession().getConnection().createStatement().execute("DELETE FROM Articles");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
