package org.im.dc.client.ui.xmlstructure.containers;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.im.dc.client.ui.xmlstructure.ArticleUIContext;
import org.im.dc.client.ui.xmlstructure.XSContainer;
import org.im.dc.client.ui.xmlstructure.XSContainersFactory;
import org.im.dc.client.ui.xmlstructure.XmlMany;

public class XSParticleContainer implements XSContainer {
    public XSParticleContainer(ArticleUIContext context, XSContainer parentContainer, XSParticle obj) {
        XSContainersFactory.createUI(context, this, obj.getTerm());
        
    }
}
