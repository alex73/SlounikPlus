package org.im.dc.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is base class for update all articles in database. It's required during xsd change. Any specific dictionary can
 * extend this class and create own implementation.
 */
public abstract class UpdateInDb {

    protected ThreadLocal<DocumentBuilder> builder = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            try {
                return DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (Exception ex) {
                return null;
            }
        }
    };
    protected ThreadLocal<Transformer> transformer = new ThreadLocal<Transformer>() {
        @Override
        protected Transformer initialValue() {
            try {
                return TransformerFactory.newInstance().newTransformer();
            } catch (Exception ex) {
                return null;
            }
        }
    };

    public UpdateInDb(String configDir) throws Exception {
        Config.load(configDir);
        Db.init();
    }

    List<Integer> articleIds;
    AtomicInteger processed = new AtomicInteger(0);

    protected Document bin2doc(byte[] bin) throws Exception {
        return builder.get().parse(new ByteArrayInputStream(bin));
    }

    protected byte[] doc2bin(Document doc) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.get().transform(new DOMSource(doc), new StreamResult(out));
        return out.toByteArray();
    }

    protected void findNodes(Element root, String nodeName, Consumer<Element> action) {
        for (Node ch = root.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (ch.getNodeName().equals(nodeName)) {
                action.accept((Element) ch);
            } else
                findNodes((Element) ch, nodeName, action);
        }
    }

    protected void findNodes1(Element root, String nodeName, Consumer<Element> action) {
        for (Node ch = root.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (ch.getNodeName().equals(nodeName)) {
                action.accept((Element) ch);
            }
        }
    }

    protected void updateAll(String articleType) throws Exception {
        Db.exec((api) -> {
            articleIds = api.getArticleMapper().selectAllIds(articleType);
        });
        System.out.println("Need to update " + articleIds.size() + " records");

        articleIds.parallelStream().forEach(id -> {
            if (!needToProcess(id)) {
                return;
            }
            try {
                Db.exec((api) -> {
                    int p = processed.incrementAndGet();
                    if (p % 1000 == 0) {
                        System.out.println(p + " done");
                    }
                    RecArticle rec = api.getArticleMapper().selectArticleForUpdate(id);
                    if (!needToProcess(rec)) {
                        return;
                    }
                    try {
                        Document doc = bin2doc(rec.getXml());
                        byte[] prevXml = doc2bin(doc);
                        doc = updateArticle(rec, doc);
                        byte[] newXml = doc2bin(doc);
                        if (Arrays.equals(prevXml, newXml)) {
                            return;
                        }
                        rec.setXml(newXml);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    Date prevUpdated = rec.getLastUpdated();
                    rec.setLastUpdated(new Date());
                    int u = api.getArticleMapper().updateArticle(rec, prevUpdated);
                    if (u != 1) {
                        throw new RuntimeException("No updated. Possible somebody other updated");
                    }
                });
            } catch (Exception ex) {
                throw new RuntimeException("Error during processing ID=" + id, ex);
            }
        });
    }

    protected boolean needToProcess(int id) {
        return true;
    }

    protected boolean needToProcess(RecArticle a) {
        return true;
    }

    abstract protected Document updateArticle(RecArticle a, Document doc) throws Exception;
}
