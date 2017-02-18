package org.im.dc.service.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ArticleFullInfo {
    // поўны артыкул
    public ArticleFull article;
    // за артыкулам сочыць гэты карыстальнік
    public boolean youWatched;
    // артыкул можа рэдагавацца гэтым карыстальнікам
    public boolean youCanEdit;
    // гэты карыстальнік можа зьмяняць стан артыкула на вызначаныя тут
    public Set<String> youCanChangeStateTo = new TreeSet<>();
    // гісторыя зменаў артыкула
    public List<Related> related = new ArrayList<>();
    // спасылаюцца на гэты артыкул
    public List<LinkFrom> links;
    // спасылкі на зьмешнія рэсурсы
    public List<String> externalLinks;

    public static class LinkFrom {
        public int articleId;
        public String[] words;
    }

    public static class Related {
        public Integer historyId;
        public Integer commentId;
        public Integer issueId;

        // калі быў зменены
        public Date when;
        // хто змяніў
        public String who;
        public String what;
    }
}
