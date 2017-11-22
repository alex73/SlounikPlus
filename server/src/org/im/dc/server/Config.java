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
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.States;
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
        for (String t : config.getTypes().getType()) {
            ArticleSchema as = new ArticleSchema();
            as.source = Files.readAllBytes(new File(CONFIG_DIR, t + ".xsd").toPath());
            as.xsdSchema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(as.source)));
            schemas.put(t, as);
        }

        LOG.info("Config loading finished");
    }

    public static State getStateByName(String articleType, String state) {
        for (States sts : config.getStates()) {
            if (sts.getType().equals(articleType)) {
                for (State s : sts.getState()) {
                    if (s.getId().equals(state)) {
                        return s;
                    }
                }
            }
        }
        return null;
    }

    public static org.im.dc.gen.config.Config getConfig() {
        return config;
    }

    public static String getConfigDir() {
        return CONFIG_DIR;
    }

    private static void checkConfig() {
        Set<String> types = new TreeSet<>();
        for (String t : config.getTypes().getType()) {
            if (!types.add(t)) {
                throw new RuntimeException("Duplicate type in config: " + t);
            }
        }
        Set<String> roles = new TreeSet<>();
        for (String r : config.getRoles().getRole()) {
            if (!roles.add(r)) {
                throw new RuntimeException("Duplicate role in config: " + r);
            }
        }
        Set<String> permissions = new TreeSet<>();
        for (Permissions ps : config.getPermissions()) {
            if (!permissions.add(ps.getType() + '/' + ps.getRole())) {
                throw new RuntimeException(
                        "Duplicate permission in config: type=" + ps.getType() + " role=" + ps.getRole());
            }
        }
        for (User u : config.getUsers().getUser()) {
            if (!roles.contains(u.getRole())) {
                throw new RuntimeException("There is no specified role:: " + u.getRole());
            }
        }
        for (States sts : config.getStates()) {
            for (State st : sts.getState()) {
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
        }

        Set<String> typeStates = new TreeSet<>();
        for (States sts : config.getStates()) {
            if (!typeStates.add(sts.getType())) {
                throw new RuntimeException("Duplicate state/type in config: " + sts.getType());
            }
            Set<String> states = new TreeSet<>();
            for (State st : sts.getState()) {
                if (!states.add(st.getId())) {
                    throw new RuntimeException("Duplicate state in config: " + st.getId());
                }
            }
            for (State st : sts.getState()) {
                for (Change ch : st.getChange()) {
                    if (!states.contains(ch.getTo())) {
                        throw new RuntimeException("There is no specified state:: " + ch.getTo());
                    }
                }
            }
        }
    }

    public static class ArticleSchema {
        public byte[] source;
        public Schema xsdSchema;
    }
}
