package org.im.dc.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.Permissions;
import org.im.dc.gen.config.Role;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.Type;
import org.im.dc.gen.config.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private static String CONFIG_DIR;

    static private org.im.dc.gen.config.Config config;

    static public Map<String, ArticleSchema> schemas;

    public static synchronized void load(String configDir) throws Exception {
        CONFIG_DIR = configDir;
        final File CONFIG_FILE = new File(CONFIG_DIR, "config.xml");

        LOG.info("Config loading start from " + CONFIG_FILE.getAbsolutePath());
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(Config.class.getResource("/org/im/dc/xsd/config.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(CONFIG_FILE));

        Unmarshaller unm = JAXBContext.newInstance(org.im.dc.gen.config.Config.class).createUnmarshaller();
        config = (org.im.dc.gen.config.Config) unm.unmarshal(CONFIG_FILE);
        try {
            checkConfig();
        } catch (Throwable ex) {
            LOG.error(ex.getMessage());
            throw ex;
        }

        schemas = new TreeMap<>();
        for (Type t : config.getTypes().getType()) {
            ArticleSchema as = new ArticleSchema();
            as.source = Files.readAllBytes(new File(CONFIG_DIR, t.getId() + ".xsd").toPath());
            as.xsdSchema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(as.source)));
            schemas.put(t.getId(), as);
        }

        LOG.info("Config loading finished");
    }

    public static org.im.dc.gen.config.Config getConfig() {
        return config;
    }

    public static String getConfigDir() {
        return CONFIG_DIR;
    }

    private static void checkConfig() {
        Set<String> roles = new TreeSet<>();
        for (Role r : config.getRoles().getRole()) {
            if (!roles.add(r.getName())) {
                throw new RuntimeException("Duplicate role in config: " + r.getName());
            }
        }
        Set<String> users = new TreeSet<>();
        for (User u : config.getUsers().getUser()) {
            if (!users.add(u.getName())) {
                throw new RuntimeException("Duplicate user in config: " + u.getName());
            }
            if (!roles.contains(u.getRole())) {
                throw new RuntimeException("There is no specified role: " + u.getRole());
            }
        }
        Set<String> states = new TreeSet<>();
        for (String st : config.getStates().getState()) {
            if (!states.add(st)) {
                throw new RuntimeException("Duplicate state in config: " + st);
            }
        }
        Set<String> typeIds = new TreeSet<>();
        Set<String> typeNames = new TreeSet<>();
        for (Type t : config.getTypes().getType()) {
            if (!typeIds.add(t.getId())) {
                throw new RuntimeException("Duplicate type in config: " + t.getId());
            }
            if (!typeNames.add(t.getName())) {
                throw new RuntimeException("Duplicate type in config: " + t.getName());
            }
            if (!states.contains(t.getNewArticleState())) {
                throw new RuntimeException("There is no specified state: " + t.getNewArticleState());
            }
            for (State st : t.getState()) {
                if (st.getEditRoles() != null) {
                    for (String r : st.getEditRoles().split(",")) {
                        if (!roles.contains(r)) {
                            throw new RuntimeException("There is no specified role:: " + r);
                        }
                    }
                }
                for (Change ch : st.getChange()) {
                    for (String r : ch.getRoles().split(",")) {
                        if (!roles.contains(r)) {
                            throw new RuntimeException("There is no specified role:: " + r);
                        }
                    }
                }
            }
            for (Permissions ps : t.getPermissions()) {
                if (!roles.contains(ps.getRole())) {
                    throw new RuntimeException("There is no specified role: " + ps.getRole());
                }
            }
        }
    }

    public static class ArticleSchema {
        public byte[] source;
        public Schema xsdSchema;
    }
}
