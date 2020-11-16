package org.im.dc.server.startup;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start/stop listener from web server.
 */
public class ApplicationInit implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInit.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            String configDir = event.getServletContext().getInitParameter("CONFIG_DIR");
            LOG.info("Initialize application from " + configDir);
            Config.load(configDir);
            Db.init(configDir);

            initPlugins();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    static void initPlugins() throws Exception {
        LOG.info("Initialize plugins...");
        Enumeration<URL> mfs = Server.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (mfs.hasMoreElements()) {
            URL url = mfs.nextElement();
            try (InputStream in = new BufferedInputStream(url.openStream())) {
                Manifest m = new Manifest(in);
                String initClass = m.getMainAttributes().getValue("SlounikPlus-init-server");
                if (initClass != null) {
                    LOG.info("  Initialize from class " + initClass);
                    Class<?> c = Class.forName(initClass);
                    Method me = c.getMethod("serverStarted");
                    me.invoke(c);
                }
            }
        }
    }
}
