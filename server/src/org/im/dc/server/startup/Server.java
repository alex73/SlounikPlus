package org.im.dc.server.startup;

import java.io.File;

import javax.xml.ws.Endpoint;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.service.impl.ArticleWebserviceImpl;

/**
 * Server startup and initialization class.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load();
        Db.init();

        Endpoint.publish("http://localhost:9080/myapp/articles", new ArticleWebserviceImpl());
        System.out.println("Server started");
    }
}
