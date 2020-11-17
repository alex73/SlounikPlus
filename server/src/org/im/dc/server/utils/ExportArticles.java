package org.im.dc.server.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.im.dc.gen.config.Type;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;

public class ExportArticles {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    static ZipOutputStream zip;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ExportArticles <dir|zip>");
            System.exit(1);
        }

        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init(System.getProperty("CONFIG_DIR"));
        File out = new File(args[0]);
        if (out.getName().toLowerCase().endsWith(".zip")) {
            out.getParentFile().mkdirs();
            zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out), 256 * 1024),
                    StandardCharsets.UTF_8);
        } else {
            out.mkdirs();
        }

        for (Type type : Config.getConfig().getTypes().getType()) {
            Db.exec((api) -> {
                List<RecArticle> articles = api.getArticleMapper().getAllArticles(type.getId());
                for (RecArticle a : articles) {
                    String fn = a.getArticleType() + '/' + a.getHeader().replace('/', '_') + '-' + a.getArticleId()
                            + ".xml";
                    System.err.println(fn);
                    byte[] xml;
                    if (a.getXml() != null) {
                        xml = xml2text(a.getXml()).getBytes("UTF-8");
                    } else {
                        xml = new byte[0];
                    }
                    if (zip != null) {
                        zip.putNextEntry(new ZipEntry(fn));
                        zip.write(xml);
                        zip.closeEntry();
                    } else {
                        Path p = new File(args[0], fn).toPath();
                        Files.createDirectories(p.getParent());
                        Files.write(p, xml);
                    }
                }
            });
        }

        if (zip != null) {
            zip.flush();
            zip.close();
        }
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
