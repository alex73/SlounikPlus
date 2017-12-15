package org.im.dc.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.XSContainersFactory;
import org.im.dc.service.dto.InitialData;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.LSInput;

public class SchemaLoader {
    private static Map<String, XSModel> models;

    public static void init(InitialData info) {
        models = new TreeMap<>();
        for (InitialData.TypeInfo ti : info.articleTypes) {
            ValidatingXMLSchemaLoader schemaLoader = new ValidatingXMLSchemaLoader();
            schemaLoader.setEntityResolver(new XMLEntityResolver() {
                @Override
                public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
                        throws XNIException, IOException {
                    String fn = resourceIdentifier.getLiteralSystemId();
                    byte[] xsd = info.xsds.get(fn);
                    if (xsd == null) {
                        throw new IOException("XSD not found: " + fn);
                    }
                    return new XMLInputSource(resourceIdentifier.getPublicId(), fn,
                            resourceIdentifier.getBaseSystemId(), new ByteArrayInputStream(xsd), null);
                }
            });
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
                    return ti.typeId;
                }

                @Override
                public String getStringData() {
                    return null;
                }

                @Override
                public String getPublicId() {
                    return ti.typeId;
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
                    byte[] xsd = info.xsds.get(ti.typeId + ".xsd");
                    return new ByteArrayInputStream(xsd);
                }

                @Override
                public String getBaseURI() {
                    return null;
                }
            });
            models.put(ti.typeId, model);
        }
    }

    public static IXSContainer createUI(ArticleUIContext context) {
        XSModel model = models.get(context.getArticleTypeId());
        XSElementDeclaration root = model.getElementDeclaration(context.getArticleTypeId(), null);
        if (root == null) {
            throw new RuntimeException("Element '" + context.getArticleTypeId() + "' is not defined in XSD.");
        }
        return XSContainersFactory.createUI(context, null, root);
    }

    public static List<String> getSimpleTypeEnumeration(String typeName, String articleTypeId) {
        XSModel model = models.get(articleTypeId);
        if (model == null) {
            throw new RuntimeException("There is no model for type '" + typeName + "'");
        }
        XSSimpleTypeDefinition type = (XSSimpleTypeDefinition) model.getTypeDefinition(typeName, null);
        if (type == null) {
            return null;
        }
        return type.getLexicalEnumeration();
    }

    public static class ValidatingXMLSchemaLoader extends XMLSchemaLoader implements XMLErrorHandler, DOMErrorHandler {
        private RuntimeException ex;

        public ValidatingXMLSchemaLoader() {
            setErrorHandler(this);
            setParameter(Constants.DOM_ERROR_HANDLER, this);
        }

        @Override
        public XSModel load(LSInput is) {
            XSModel m = super.load(is);
            if (ex != null) {
                throw ex;
            }
            return m;
        }

        @Override
        public boolean handleError(DOMError error) {
            ex = new RuntimeException(error.getMessage());
            ex.printStackTrace();
            return false;
        }

        @Override
        public void warning(String domain, String key, XMLParseException exception) throws XNIException {
            ex = exception;
            ex.printStackTrace();
        }

        @Override
        public void fatalError(String domain, String key, XMLParseException exception) throws XNIException {
            ex = exception;
            ex.printStackTrace();
        }

        @Override
        public void error(String domain, String key, XMLParseException exception) throws XNIException {
            ex = exception;
            ex.printStackTrace();
        }
    }
}
