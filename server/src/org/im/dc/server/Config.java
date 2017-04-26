package org.im.dc.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.Role;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    static final File CONFIG_FILE = new File("config/config.xml");
    static final File ARTICLE_SCHEMA_FILE = new File("config/article.xsd");

    static private org.im.dc.gen.config.Config config;

    static public byte[] articleSchemaSource;
    static public Schema articleSchema;

    public static void load() throws Exception {
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

        schemaFactory.newSchema(ARTICLE_SCHEMA_FILE);
        articleSchemaSource = Files.readAllBytes(ARTICLE_SCHEMA_FILE.toPath());
        articleSchema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(articleSchemaSource)));
        LOG.info("Config loading finished");
    }

    public static State getStateByName(String state) {
        for (State s : config.getStates().getState()) {
            if (s.getId().equals(state)) {
                return s;
            }
        }
        return null;
    }

    public static org.im.dc.gen.config.Config getConfig() {
        return config;
    }

    private static void checkConfig() {
        Set<String> roles = new TreeSet<>();
        for (Role r : config.getRoles().getRole()) {
            if (!roles.add(r.getId())) {
                throw new RuntimeException("Duplicate role in config: " + r.getId());
            }
        }
        for (User u : config.getUsers().getUser()) {
            if (!roles.contains(u.getRole())) {
                throw new RuntimeException("There is no specified role:: " + u.getRole());
            }
        }
        for (State st : config.getStates().getState()) {
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

        Set<String> states = new TreeSet<>();
        for (State st : config.getStates().getState()) {
            if (!states.add(st.getId())) {
                throw new RuntimeException("Duplicate state in config: " + st.getId());
            }
        }
        for (State st : config.getStates().getState()) {
            for (Change ch : st.getChange()) {
                if (!states.contains(ch.getTo())) {
                    throw new RuntimeException("There is no specified state:: " + ch.getTo());
                }
            }
        }
    }
}
