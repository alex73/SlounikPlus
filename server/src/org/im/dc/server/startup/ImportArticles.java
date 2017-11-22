package org.im.dc.server.startup;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;

public class ImportArticles {
    static Date lastUpdated;
    static List<RecArticle> articles;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ImportArticles <dir|zip>");
            System.exit(1);
        }

        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init();

        lastUpdated = new Date();
        articles = new ArrayList<>();

        File in = new File(args[0]);
        if (in.getName().toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(in)))) {
                for (ZipEntry en = zip.getNextEntry(); en != null; en = zip.getNextEntry()) {
                    if (en.getName().toLowerCase().endsWith(".xml")) {
                        read(en.getName().toLowerCase(), null, IOUtils.toByteArray(zip));
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
                    read(f.getName().toLowerCase(), null, FileUtils.readFileToByteArray(f));
                }
            }
        }

        Db.exec((api) -> {
            api.getArticleMapper().insertArticles(articles);
        });
    }

    static void read(String articleType, String fn, byte[] xml) throws Exception {
        String header = fn.replaceAll("\\.xml$", "");

        try {
            Validator validator = Config.schemas.get(articleType).xsdSchema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
        } catch (Exception ex) {
            System.err.println("Error xml validation from " + fn);
            ex.printStackTrace();
            throw ex;
        }

        RecArticle a = new RecArticle();
        a.setHeader(header);
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
