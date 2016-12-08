package org.im.dc.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.im.dc.service.AppConst;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.dto.Header;

public class WS {
    private static ArticleWebservice articleService;
    public static Header header;

    public static void init(String url, String user, String pass) throws Exception {
        QName qname = new QName("http://impl.service.dc.im.org/", "ArticleWebserviceImplService");
        Service service = Service.create(new URL(url), qname);
        articleService = service.getPort(ArticleWebservice.class);

        header = new Header();
        header.user = user;
        header.pass = pass;
        header.appVersion = AppConst.APP_VERSION;
    }

    public static ArticleWebservice getArticleService() {
        return articleService;
    }
}
