package org.im.dc.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
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
import org.im.dc.gen.config.Config;
import org.im.dc.gen.config.Permissions;
import org.im.dc.gen.config.Role;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.Type;
import org.im.dc.gen.config.User;
import org.im.dc.service.dto.InitialData;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ConfigLoad {

    public static Config loadConfig(File configFile) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(ConfigLoad.class.getResource("/org/im/dc/xsd/config.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(configFile));

        Unmarshaller unm = JAXBContext.newInstance(org.im.dc.gen.config.Config.class).createUnmarshaller();
        Config config = (Config) unm.unmarshal(configFile);
        checkConfig(config);
        return config;
    }

    public static Map<String, byte[]> loadSchemaSources(File configDir) throws Exception {
        Map<String, byte[]> result = new TreeMap<>();
        for (File f : configDir.listFiles(f -> f.isFile() && f.getName().endsWith(".xsd"))) {
            result.put(f.getName(), Files.readAllBytes(f.toPath()));
        }
        return result;
    }

    public static Map<String, Schema> loadSchemas(Map<String, byte[]> schemaSources, List<Type> types)
            throws Exception {
        Map<String, Schema> schemas = new TreeMap<>();

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(new LSResourceResolver() {
            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
                    String baseURI) {
                return new LSInput() {
                    @Override
                    public void setSystemId(String systemId) {
                    }

                    @Override
                    public void setStringData(String stringData) {
                    }

                    @Override
                    public void setPublicId(String publicId) {
                    }

                    @Override
                    public void setEncoding(String encoding) {
                    }

                    @Override
                    public void setCharacterStream(Reader characterStream) {
                    }

                    @Override
                    public void setCertifiedText(boolean certifiedText) {
                    }

                    @Override
                    public void setByteStream(InputStream byteStream) {
                    }

                    @Override
                    public void setBaseURI(String baseURI) {
                    }

                    @Override
                    public String getSystemId() {
                        return systemId;
                    }

                    @Override
                    public String getStringData() {
                        return null;
                    }

                    @Override
                    public String getPublicId() {
                        return publicId;
                    }

                    @Override
                    public String getEncoding() {
                        return null;
                    }

                    @Override
                    public Reader getCharacterStream() {
                        return null;
                    }

                    @Override
                    public boolean getCertifiedText() {
                        return false;
                    }

                    @Override
                    public InputStream getByteStream() {
                        byte[] bytes = schemaSources.get(systemId);
                        if (bytes == null) {
                            throw new RuntimeException("There is no resource: " + systemId);
                        }
                        return new ByteArrayInputStream(bytes);
                    }

                    @Override
                    public String getBaseURI() {
                        return baseURI;
                    }
                };
            }
        });

        for (Type t : types) {
            byte[] source = schemaSources.get(t.getId() + ".xsd");
            if (source == null) {
                throw new Exception("Schema not defined for type " + t.getName());
            }
            Schema xsd = schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(source)));
            schemas.put(t.getId(), xsd);
        }
        return schemas;
    }

    public static InitialData config2initialData(Config config, String user, Map<String, byte[]> schemaSources) {
        InitialData result = new InitialData();
        result.configVersion = config.getVersion();
        result.headerLocale = config.getHeaderLocale();
        result.stress = config.getStress();
        result.xsds = schemaSources;
        for (Type type : config.getTypes().getType()) {
            InitialData.TypeInfo ti = new InitialData.TypeInfo();
            ti.typeId = type.getId();
            ti.typeName = type.getName();
            ti.newArticleState = PermissionChecker.getNewArticleState(config, ti.typeId);
            result.articleTypes.add(ti);
        }
        result.currentUserPermissions = PermissionChecker.getUserPermissions(config, user);
        Map<String, Set<String>> ps = PermissionChecker.getUserPermissionsByType(config, user);
        for (Type type : config.getTypes().getType()) {
            Set<String> permissionsList = ps.get(type.getId());
            if (permissionsList != null) {
                result.getTypeInfo(type.getId()).currentUserTypePermissions.addAll(permissionsList);
            }
        }
        result.states.addAll(config.getStates().getState());
        result.allUsers = new TreeMap<>();
        for (User u : config.getUsers().getUser()) {
            result.allUsers.put(u.getName(), u.getRole());
        }
        result.currentUserRole = PermissionChecker.getUserRole(config, user);

        return result;
    }

    private static void checkConfig(Config config) {
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
}
