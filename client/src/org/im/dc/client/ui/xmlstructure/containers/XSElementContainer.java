package org.im.dc.client.ui.xmlstructure.containers;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.im.dc.client.ui.xmlstructure.ArticleUIContext;
import org.im.dc.client.ui.xmlstructure.XSContainer;
import org.im.dc.client.ui.xmlstructure.XSContainersFactory;

// see XmlGroup
public class XSElementContainer implements XSContainer {

    public XSElementContainer(ArticleUIContext context, XSContainer parentContainer, XSElementDeclaration obj) {
        XSContainersFactory.createUI(context, this, obj.getTypeDefinition());
    }
}
