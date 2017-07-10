package org.im.dc.server.startup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

import javax.xml.ws.Endpoint;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.service.impl.ArticleWebserviceImpl;
import org.im.dc.service.impl.ToolsWebserviceImpl;

/**
 * Server startup and initialization class.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Server <addr>, where addr~=http://localhost:9081/myapp");
            System.exit(1);
        }

        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load();
        Db.init();

        initPlugins();

        Endpoint.publish(args[0] + "/articles", new ArticleWebserviceImpl());
        Endpoint.publish(args[0] + "/tools", new ToolsWebserviceImpl());
        System.out.println("Server started");
    }

    static void initPlugins() throws Exception {
        Enumeration<URL> mfs = Server.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (mfs.hasMoreElements()) {
            URL url = mfs.nextElement();
            try (InputStream in = new BufferedInputStream(url.openStream())) {
                Manifest m = new Manifest(in);
                String initClass = m.getMainAttributes().getValue("DictCreator-init");
                if (initClass != null) {
                    Class<?> c = Class.forName(initClass);
                    Method me = c.getMethod("serverInit");
                    me.invoke(c);
                }
            }
        }
    }
}
