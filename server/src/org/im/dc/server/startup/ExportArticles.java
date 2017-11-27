package org.im.dc.server.startup;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
        Db.init();
        File out = new File(args[0]);
        if (out.getName().toLowerCase().endsWith(".zip")) {
            out.getParentFile().mkdirs();
            zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out), 256 * 1024), StandardCharsets.UTF_8);
        } else {
            out.mkdirs();
        }

        Db.exec((api) -> {
            for (int id : api.getArticleMapper().selectAllIds()) {
                RecArticle a = api.getArticleMapper().selectArticle(id);
                if (a.getXml() == null) {
                    continue;
                }
                try {
                    String fn = a.getArticleType() + '/' + a.getHeader().replace('/', '_') + '-' + a.getArticleId()
                            + ".xml";
                    System.err.println(fn);
                    byte[] xml = xml2text(a.getXml()).getBytes("UTF-8");
                    if (zip != null) {
                        zip.putNextEntry(new ZipEntry(fn));
                        zip.write(xml);
                        zip.closeEntry();
                    } else {
                        Files.write(new File(args[0], fn).toPath(), xml);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

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
