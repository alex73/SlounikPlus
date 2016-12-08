package org.im.dc.service;

import javax.jws.WebService;

import org.im.dc.service.dto.Article;
import org.im.dc.service.dto.Header;

@WebService
public interface ArticleWebservice {
    Article getArticle(Header header, int id);
}
