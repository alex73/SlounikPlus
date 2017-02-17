package org.im.dc.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.im.dc.service.AppConst;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.Header;

public class WS {
    private static ArticleWebservice articleService;
    private static ToolsWebservice toolsWebservice;
    public static Header header;

    public static void init(String urlPrefix, String user, String pass) throws Exception {
        Service service;

        service = Service.create(new URL(urlPrefix + "/articles?wsdl"),
                new QName("http://impl.service.dc.im.org/", "ArticleWebserviceImplService"));
        articleService = service.getPort(ArticleWebservice.class);

        service = Service.create(new URL(urlPrefix + "/tools?wsdl"),
                new QName("http://impl.service.dc.im.org/", "ToolsWebserviceImplService"));
        toolsWebservice = service.getPort(ToolsWebservice.class);

        header = new Header();
        header.user = user;
        header.pass = pass;
        header.appVersion = AppConst.APP_VERSION;
    }

    public static ArticleWebservice getArticleService() {
        return articleService;
    }

    public static ToolsWebservice getToolsWebservice() {
        return toolsWebservice;
    }
}
