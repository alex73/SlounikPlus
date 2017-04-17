package org.im.dc.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.w3c.dom.ls.LSInput;

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;

public class SchemaLoader {
    private static XSModel model;

    public static void init(byte[] schema) {
        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();

        model = schemaLoader.load(new LSInput() {
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
                return null;
            }

            @Override
            public String getStringData() {
                return null;
            }

            @Override
            public String getPublicId() {
                return null;
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
                return new ByteArrayInputStream(schema);
            }

            @Override
            public String getBaseURI() {
                return null;
            }
        });

    }

    public static XmlGroup createUI() {
        XSElementDeclaration root = model.getElementDeclaration("root", null);
        return new XmlGroup(null, null, root, new AnnotationInfo(root.getAnnotation()));
    }

    public static List<String> getSimpleTypeEnumeration(String typeName) {
        XSSimpleTypeDefinition type = (XSSimpleTypeDefinition) model.getTypeDefinition(typeName, null);
        if (type == null) {
            return null;
        }
        return type.getLexicalEnumeration();
    }
}
