package org.im.dc.server.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;

public class ImportArticles {
    static Date lastUpdated;
    static List<RecArticle> articles;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("ImportArticles <dir|zip> <article_type>");
            System.exit(1);
        }

        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init(System.getProperty("CONFIG_DIR"));

        lastUpdated = new Date();
        articles = new ArrayList<>();

        File in = new File(args[0]);
        if (in.getName().toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(in)))) {
                for (ZipEntry en = zip.getNextEntry(); en != null; en = zip.getNextEntry()) {
                    if (en.getName().toLowerCase().endsWith(".xml")) {
                        read(args[1], en.getName().toLowerCase(), IOUtils.toByteArray(zip));
                    }
                    zip.closeEntry();
                }
            }
        } else {
            File[] ls = in.listFiles();
            if (ls == null) {
                return;
            }
            for (File f : ls) {
                if (f.isFile() && f.getName().toLowerCase().endsWith(".xml")) {
                    read(args[1], f.getName().toLowerCase(), FileUtils.readFileToByteArray(f));
                }
            }
        }

        Db.exec((api) -> {
            for (int i = 0; i < articles.size(); i += 1000) {
                System.out.println("Load from " + i + " to " + (i + 1000) + " from " + articles.size());
                api.getArticleMapper().insertArticles(articles.subList(i, Math.min(i + 1000, articles.size())));
            }
        });
    }

    static final Pattern RE_FILE = Pattern.compile("(.*)\\-([0-9]+)\\.xml");

    static void read(String articleType, String fn, byte[] xml) throws Exception {
        Matcher m = RE_FILE.matcher(fn);
        if (!m.matches()) {
            throw new Exception("Wrong file name: " + fn);
        }
        String header = m.group(1);
        int id = Integer.parseInt(m.group(2));

        try {
            //Validator validator = Config.schemas.get(articleType).newValidator();
            //validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
        } catch (Exception ex) {
            System.err.println("Error xml validation from " + fn);
            ex.printStackTrace();
            throw ex;
        }

        RecArticle a = new RecArticle();
        a.setArticleId(id);
        a.setHeader(header);
        a.setArticleType(articleType);
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
