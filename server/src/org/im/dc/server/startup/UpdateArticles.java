package org.im.dc.server.startup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;

public class UpdateArticles {
    static final Pattern RE_FILE = Pattern.compile("(.+)\\-([0-9]+)\\.xml");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("UpdateArticles <dir>");
            System.exit(1);
        }

        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init();
        File[] ls = new File(args[0]).listFiles();
        if (ls == null) {
            return;
        }

        List<RecArticle> articles = new ArrayList<>();
        for (File f : ls) {
            Matcher m = RE_FILE.matcher(f.getName());
            if (f.isFile() && m.matches()) {
                String header = m.group(1);
                int id = Integer.parseInt(m.group(2));
                byte[] xml = Files.readAllBytes(f.toPath());

                try {
                    Validator validator = Config.schemas.get(null).xsdSchema.newValidator();
                    validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
                } catch (Exception ex) {
                    System.err.println("Error xml validation from " + f);
                    ex.printStackTrace();
                    throw ex;
                }

                RecArticle a = new RecArticle();
                a.setArticleId(id);
                a.setHeader(header);
                a.setXml(xml);
                articles.add(a);
                Db.exec((api) -> {
                    if (1 != api.getArticleMapper().updateArticleWordsXml(a)) {
                        System.err.println("Can't update from " + f);
                    }
                });
            }
        }
    }
}
