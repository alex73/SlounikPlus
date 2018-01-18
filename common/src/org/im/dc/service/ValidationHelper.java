package org.im.dc.service;

import java.util.ArrayList;
import java.util.List;

public class ValidationHelper {
    private final int currentArticleId;
    private final List<String> links = new ArrayList<>();
    public String newHeader;
    public String error;

    public ValidationHelper(int currentArticleId) {
        this.currentArticleId = currentArticleId;
    }

    public void addLink(String link) {
        links.add(link);
    }

    public void replaceHeader(String newHeader) {
        this.newHeader = newHeader;
    }

    public String[] getLinks() {
        return links.toArray(new String[0]);
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