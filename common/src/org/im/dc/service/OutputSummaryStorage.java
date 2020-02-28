package org.im.dc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage for some intermediate date for validation summary.
 */
public class OutputSummaryStorage {
    public final Map<Integer, String> headers = new HashMap<>();
    public final Map<Integer, String> textForSearch = new HashMap<>();
    public final Map<Integer, String[]> linkedTo = new HashMap<>();

    /*
     * Stored as lists for be able to sort in script.
     */
    public final List<String> summaryErrors = new ArrayList<>();
    public final List<ArticleOutput> outputs = new ArrayList<>();
    public final List<ArticleError> errors = new ArrayList<>();

    public void addSummaryError(String error) {
        summaryErrors.add(error);
    }

    public static class ArticleOutput {
        public int articleId;
        public String key;
        public String html;
    }

    public static class ArticleError {
        public int articleId;
        public String key;
        public String error;
    }
}
