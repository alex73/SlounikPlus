package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.impl.js.HtmlOut;
import org.im.dc.service.impl.js.JsDomWrapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

public class ValidationHelper {
    private final OutputSummaryStorage storage;
    private final int currentArticleId;
    private final Validator validator;
    private final byte[] xml;

    public ValidationHelper(int currentArticleId, Validator validator, byte[] xml, OutputSummaryStorage storage) {
        this.storage = storage;
        this.currentArticleId = currentArticleId;
        this.validator = validator;
        this.xml = xml;
    }

    public void setLinks(String[] links) {
        storage.linkedTo.put(currentArticleId, links);
    }

    public void setHeader(String header) {
        storage.headers.put(currentArticleId, header);
    }

    public int getCurrentArticleId() {
        return currentArticleId;
    }

    public void validateByXSD() throws Exception {
        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
        } catch (SAXParseException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void articleError(String error) {
        String h = storage.headers.get(currentArticleId);
        if (h == null) {
            h = "<...>";
        }
        error("[#" + currentArticleId + ":" + h + "]", error);
    }

    public void error(String key, String error) {
        OutputSummaryStorage.ArticleError ae = new OutputSummaryStorage.ArticleError();
        ae.articleId = currentArticleId;
        ae.key = key;
        ae.error = error;
        storage.errors.add(ae);
    }

    public void output(String key, String html) {
        OutputSummaryStorage.ArticleOutput ao = new OutputSummaryStorage.ArticleOutput();
        ao.articleId = currentArticleId;
        ao.key = key;
        ao.html = html;
        storage.outputs.add(ao);
    }

    public void log(String text) {
        System.out.println(text);
    }

    public HtmlOut createHtmlOut() {
        return new HtmlOut();
    }

    public Map<String, Object> loadDictionary(String articleType) throws Exception {
        Map<String, byte[]> articles = Db.execAndReturn((api) -> {
            List<RecArticle> existArticles = api.getArticleMapper().getAllArticles(articleType);
            Map<String, byte[]> xmls = new HashMap<>();
            for (RecArticle a : existArticles) {
                xmls.put(a.getHeader(), a.getXml());
            }
            return xmls;
        });
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, byte[]> en : articles.entrySet()) {
            Document doc = JsDomWrapper.parseDoc(en.getValue());
            result.put(en.getKey(), new JsDomWrapper(doc.getDocumentElement()));
        }
        return result;
    }

    /*
     * public boolean checkUniqueWords(String[] words) throws Exception { List<String> str = new
     * ArrayList<>(words.length); for (String w : words) { str.add(w); } return Db.execAndReturn((api) -> {
     * List<RecArticle> existArticles = api.getArticleMapper().getArticlesWithWords(str); if (existArticles.isEmpty()) {
     * return true; } if (existArticles.size() == 1 && existArticles.get(0).getArticleId() == currentArticleId) { return
     * true; }
     * 
     * return false; }); }
     * 
     * public boolean checkExistWord(String word) throws Exception { return Db.execAndReturn((api) -> { List<RecArticle>
     * existArticles = api.getArticleMapper().getArticlesWithWords(Arrays.asList(word)); return
     * !existArticles.isEmpty(); }); }
     */
}
