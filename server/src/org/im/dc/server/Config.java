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

import org.im.dc.gen.config.Permission;
import org.im.dc.gen.config.Role;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.User;

public class Config {
    static final File CONFIG_FILE = new File("config/config.xml");
    static final File ARTICLE_SCHEMA_FILE = new File("config/article.xsd");

    static private org.im.dc.gen.config.Config config;

    static public byte[] articleSchemaSource;
    static public Schema articleSchema;

    public static void load() throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(Config.class.getResource("/org/im/dc/xsd/config.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(CONFIG_FILE));

        Unmarshaller unm = JAXBContext.newInstance(org.im.dc.gen.config.Config.class).createUnmarshaller();
        config = (org.im.dc.gen.config.Config) unm.unmarshal(CONFIG_FILE);
        //TODO check role names

        schemaFactory.newSchema(ARTICLE_SCHEMA_FILE);
        articleSchemaSource = Files.readAllBytes(ARTICLE_SCHEMA_FILE.toPath());
        articleSchema = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(articleSchemaSource)));
    }

    public static boolean checkUser(String user, String pass) {
        for (User u : config.getUsers().getUser()) {
            if (u.getName().equals(user) && u.getPass().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPerm(String user, Permission perm) {
        String userRole = getUserRole(user);
        for (Role r : config.getRoles().getRole()) {
            if (r.getId().equals(userRole)) {
                for (Permission p : r.getPermission()) {
                    if (perm.equals(p)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getUserRole(String user) {
        for (User u : config.getUsers().getUser()) {
            if (u.getName().equals(user)) {
                return u.getRole();
            }
        }
        return null;
    }

    public static Set<String> getUserPermissions(String user) {
        String userRole = getUserRole(user);
        Set<String> result = new TreeSet<>();
        for (Role r : config.getRoles().getRole()) {
            if (r.getId().equals(userRole)) {
                for (Permission p : r.getPermission()) {
                    result.add(p.name());
                }
            }
        }
        return result;
    }

    public static State getStateByName(String state) {
        for (State s : config.getStates().getState()) {
            if (s.getId().equals(state)) {
                return s;
            }
        }
        return null;
    }

    public static boolean roleInRolesList(String role, String rolesList) {
        if (rolesList == null) {
            return false;
        }
        for (String r : rolesList.split(",")) {
            if (r.trim().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public static org.im.dc.gen.config.Config getConfig() {
        return config;
    }
}
