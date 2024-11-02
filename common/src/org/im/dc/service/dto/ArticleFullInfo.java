package org.im.dc.service.dto;

import java.util.ArrayList;
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
    // карыстальнік можа прапаноўваць змены
    public boolean youCanProposeChanges;
    // гэты карыстальнік можа зьмяняць стан артыкула на вызначаныя тут
    public Set<String> youCanChangeStateTo = new TreeSet<>();
    // гісторыя зменаў артыкула
    public List<Related> related = new ArrayList<>();
    // спасылаюцца на гэты артыкул
    public List<LinkFrom> linksFrom = new ArrayList<>();
    // спасылкі на зьнешнія рэсурсы
    public List<LinkExternal> linksExternal = new ArrayList<>();

    public static class LinkFrom {
        public String articleType;
        public int articleId;
        public String header;
    }

    public static class LinkExternal {
        public String name;
        public String url;
    }
}
