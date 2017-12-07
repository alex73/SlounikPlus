package org.im.dc.client.ui.xmlstructure.containers;

import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.im.dc.client.ui.xmlstructure.ArticleUIContext;
import org.im.dc.client.ui.xmlstructure.XSContainer;
import org.im.dc.client.ui.xmlstructure.XSContainersFactory;

public class XSComplexTypeContainer implements XSContainer {
    public XSComplexTypeContainer(ArticleUIContext context, XSContainer parentContainer, XSComplexTypeDefinition obj) {
        XSObjectList attrList = obj.getAttributeUses();
        for (int i = 0; i < attrList.getLength(); i++) {
            XSObject o = attrList.item(i);
            if (o instanceof XSAttributeUse) {
                XSAttributeUse attr = (XSAttributeUse) o;
                XSContainersFactory.createUI(context, this, attr.getAttrDeclaration());
            } else {
                throw new RuntimeException("Unknown attribute declaration");
            }
        }
        if (obj.getParticle() != null) {
            XSContainersFactory.createUI(context, this, obj.getParticle());
        }
    }
}
