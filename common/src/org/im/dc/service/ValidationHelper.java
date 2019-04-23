package org.im.dc.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

public class ValidationHelper {
    public static final String KNOWN_ERRORS_PREFIX = "SlounikPlus:";

    private final int currentArticleId;
    private final Validator validator;
    private final byte[] xml;
    private final List<String> links = new ArrayList<>();
    public String newHeader;
    public String error;

    public ValidationHelper(int currentArticleId, Validator validator, byte[] xml) {
        this.currentArticleId = currentArticleId;
        this.validator = validator;
        this.xml = xml;
    }

    public void addLink(String link) {
        links.add(link);
    }

    public void replaceHeader(String newHeader) {
        this.newHeader = newHeader;
    }

    public int getCurrentArticleId() {
        return currentArticleId;
    }

    public String[] getLinks() {
        return links.toArray(new String[0]);
    }

    public void validateByXSD() throws Exception {
        validator.validate(new StreamSource(new ByteArrayInputStream(xml)));
    }

    public void log(String text) {
        System.out.println(text);
    }

    /*public boolean checkUniqueWords(String[] words) throws Exception {
        List<String> str = new ArrayList<>(words.length);
        for (String w : words) {
            str.add(w);
        }
        return Db.execAndReturn((api) -> {
            List<RecArticle> existArticles = api.getArticleMapper().getArticlesWithWords(str);
            if (existArticles.isEmpty()) {
                return true;
            }
            if (existArticles.size() == 1 && existArticles.get(0).getArticleId() == currentArticleId) {
                return true;
            }

            return false;
        });
    }

    public boolean checkExistWord(String word) throws Exception {
        return Db.execAndReturn((api) -> {
            List<RecArticle> existArticles = api.getArticleMapper().getArticlesWithWords(Arrays.asList(word));
            return !existArticles.isEmpty();
        });
    }*/
}
