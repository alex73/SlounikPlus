package org.im.dc.client.ui.xmlstructure.containers;

import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.im.dc.client.ui.xmlstructure.ArticleUIContext;
import org.im.dc.client.ui.xmlstructure.XSContainer;
import org.im.dc.client.ui.xmlstructure.XSContainersFactory;

public class XSModelGroupContainer implements XSContainer {
    public XSModelGroupContainer(ArticleUIContext context, XSContainer parentContainer, XSModelGroup obj) {
        switch (obj.getCompositor()) {
        case XSModelGroup.COMPOSITOR_SEQUENCE:
            XSObjectList sq = obj.getParticles();
            for (int i = 0; i < sq.getLength(); i++) {
                XSObject part = sq.item(i);
                XSContainersFactory.createUI(context, this, part);
            }
            break;
        case XSModelGroup.COMPOSITOR_CHOICE:
            XSObjectList ch = obj.getParticles();
            for (int i = 0; i < ch.getLength(); i++) {
                XSObject part = ch.item(i);
                XSContainersFactory.createUI(context, this, part);
            }
            break;
        case XSModelGroup.COMPOSITOR_ALL:
            throw new RuntimeException("ALL compositor is not supported yet");
        default:
            throw new RuntimeException("Unknown compositor");
        }
    }
}
