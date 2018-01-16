package org.im.dc.server;

import java.io.File;
import java.util.Map;

import javax.xml.validation.Schema;

import org.im.dc.config.ConfigLoad;
import org.im.dc.service.AppConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private static String CONFIG_DIR;

    static private org.im.dc.gen.config.Config config;

    static public Map<String, byte[]> schemaSources;
    static public Map<String, Schema> schemas;

    public static synchronized void load(String configDir) throws Exception {
        CONFIG_DIR = configDir;
        final File CONFIG_FILE = new File(CONFIG_DIR, "config.xml");

        LOG.info("Config loading start from " + CONFIG_FILE.getAbsolutePath());
        try {
            config = ConfigLoad.loadConfig(CONFIG_FILE);
            if (!Integer.toString(AppConst.APP_VERSION).equals(config.getAppVersion())) {
                throw new Exception("Config has wrong app version");
            }
            schemaSources = ConfigLoad.loadSchemaSources(new File(CONFIG_DIR));
            schemas = ConfigLoad.loadSchemas(schemaSources, config.getTypes().getType());
        } catch (Throwable ex) {
            LOG.error(ex.getMessage());
            throw ex;
        }

        LOG.info("Config loading finished");
    }

    public static org.im.dc.gen.config.Config getConfig() {
        return config;
    }

    public static String getConfigDir() {
        return CONFIG_DIR;
    }
}
