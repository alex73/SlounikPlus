package org.im.dc.server;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.User;

public class Config {
    static final File CONFIG_FILE = new File("config/config.xml");

    static private org.im.dc.gen.config.Config config;

    public static void load() throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(Config.class.getResource("/org/im/dc/xsd/config.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(CONFIG_FILE));

        Unmarshaller unm = JAXBContext.newInstance(org.im.dc.gen.config.Config.class).createUnmarshaller();
        config = (org.im.dc.gen.config.Config) unm.unmarshal(CONFIG_FILE);
    }

    public static boolean checkUser(String user, String pass) {
        for (User u : config.getUsers().getUser()) {
            if (u.getName().equals(user) && u.getPass().equals(pass)) {
                return true;
            }
        }
        return false;
    }
}
