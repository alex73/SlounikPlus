package org.im.dc.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;

import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.w3c.dom.ls.LSInput;

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModel;

public class SchemaLoader {
    private static XSElementDeclaration root;

    public static void init(byte[] schema) {
        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();

        XSModel model = schemaLoader.load(new LSInput() {
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
        root = model.getElementDeclaration("root", null);
    }

    public static XmlGroup createUI() {
        return new XmlGroup(root, new AnnotationInfo(root.getAnnotation()));
    }
}
