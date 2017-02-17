package org.im.dc.service.dto;

import java.util.List;

public class ArticleFullInfo {
    public ArticleFull article;
    public boolean watchedByYou;
    public List<ArticleHistory> history;
    public List<LinkFrom> links;
    public List<String> externalLinks;

    public static class LinkFrom {
        public int articleId;
        public String[] words;
    }

    public static class ArticleHistory {
        public int historyId;
    }
}
