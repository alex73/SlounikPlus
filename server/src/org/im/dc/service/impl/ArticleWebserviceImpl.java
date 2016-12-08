package org.im.dc.service.impl;

import javax.jws.WebService;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.service.AppConst;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.dto.Article;
import org.im.dc.service.dto.Header;

@WebService(endpointInterface = "org.im.dc.service.ArticleWebservice")
public class ArticleWebserviceImpl implements ArticleWebservice {

    private void check(Header header) {
        if (header.appVersion != AppConst.APP_VERSION) {
            throw new RuntimeException("Wrong app version");
        }
        if (!Config.checkUser(header.user, header.pass)) {
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public Article getArticle(Header header, int id) {
        check(header);

        RecArticle rec = Db.exec((api) -> api.getSelector().selectArticle(id));
        if (rec == null) {
            return null;
        }

        Article a = new Article();
        a.id = rec.getId();
        a.words = rec.getWords();
        return a;
    }
}
