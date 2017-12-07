package org.im.dc.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.ArticleUIContext;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.service.dto.InitialData;
import org.w3c.dom.ls.LSInput;

public class SchemaLoader {
    private static Map<String, XSModel> models;

    public static void init(List<InitialData.TypeInfo> articleTypes) {
        models = new TreeMap<>();
        for (InitialData.TypeInfo ti : articleTypes) {
            XMLSchemaLoader schemaLoader = new XMLSchemaLoader();

            models.put(ti.typeId, schemaLoader.load(new LSInput() {
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
                    return new ByteArrayInputStream(ti.articleSchema);
                }

                @Override
                public String getBaseURI() {
                    return null;
                }
            }));
        }
    }

    public static XmlGroup createUI(ArticleUIContext context) {
        XSElementDeclaration root = models.get(context.getArticleTypeId())
                .getElementDeclaration(context.getArticleTypeId(), null);
        if (root == null) {
            throw new RuntimeException("Element '" + context.getArticleTypeId() + "' is not defined in XSD.");
        }
        return new XmlGroup(context, null, root, new AnnotationInfo(root.getAnnotation(), root.getName()), true);
    }

    public static List<String> getSimpleTypeEnumeration(String typeName, String articleTypeId) {
        XSSimpleTypeDefinition type = (XSSimpleTypeDefinition) models.get(articleTypeId).getTypeDefinition(typeName,
                null);
        if (type == null) {
            return null;
        }
        return type.getLexicalEnumeration();
    }
}
