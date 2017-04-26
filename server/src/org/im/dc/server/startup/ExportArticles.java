package org.im.dc.server.startup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Arrays;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;

public class ExportArticles {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ExportArticles <dir>");
            System.exit(1);
        }

        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
        new File(args[0]).mkdirs();

        Db.exec((api) -> {
            for (int id : api.getArticleMapper().selectAllIds()) {
                RecArticle a = api.getArticleMapper().selectArticle(id);
                if (a.getXml() == null) {
                    continue;
                }
                try {
                    String fn = Arrays.toString(a.getWords()).replaceAll("^\\[", "").replaceAll("\\]$", "").replace('/',
                            '_') + '-' + a.getArticleId() + ".xml";
                    System.err.println(fn);
                    Files.write(new File(args[0], fn).toPath(), xml2text(a.getXml()).getBytes("UTF-8"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static String xml2text(byte[] xml) throws Exception {
        Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamResult res = new StreamResult(new StringWriter());
        Source sou = new StreamSource(new ByteArrayInputStream(xml));
        transformer.transform(sou, res);
        return res.getWriter().toString();
    }
}
