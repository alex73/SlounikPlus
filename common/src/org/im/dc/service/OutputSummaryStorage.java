package org.im.dc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Storage for some intermediate date for validation summary.
 */
public class OutputSummaryStorage {
    public final Map<Integer, ArticleInfo> articleInfos = new HashMap<>();
    public final List<String> summaryErrors = new ArrayList<>();

    public ArticleInfo getArticleInfo(int articleId) {
        ArticleInfo ai = articleInfos.get(articleId);
        if (ai == null) {
            ai = new ArticleInfo();
            articleInfos.put(articleId, ai);
        }
        return ai;
    }

    public void addSummaryError(String error) {
        summaryErrors.add(error);
    }

    public static class ArticleInfo {
        public int articleId;
        public String key; // key for find unique
        public String header; // header in article db
        public String textForSearch;
        public Set<String> linkedTo = new TreeSet<>();
        public List<ArticleOutput> outputs = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
    }

    public static class ArticleOutput {
        public String key;
        public String html;
    }
}
