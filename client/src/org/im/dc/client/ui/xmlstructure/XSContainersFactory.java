package org.im.dc.client.ui.xmlstructure;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSWildcard;
import org.im.dc.client.ui.xmlstructure.containers.XSAttributeContainer;
import org.im.dc.client.ui.xmlstructure.containers.XSComplexTypeContainer;
import org.im.dc.client.ui.xmlstructure.containers.XSElementContainer;
import org.im.dc.client.ui.xmlstructure.containers.XSModelGroupContainer;
import org.im.dc.client.ui.xmlstructure.containers.XSParticleContainer;
import org.im.dc.client.ui.xmlstructure.containers.XSSimpleTypeContainer;

public class XSContainersFactory {
    public static XSContainer createUI(ArticleUIContext context, XSContainer parent, XSObject xsObject) {
        if (xsObject instanceof XSElementDeclaration) {
            System.out.println("XSElementContainer for "+xsObject.getName());
            return new XSElementContainer(context, parent, (XSElementDeclaration) xsObject);
        } else if (xsObject instanceof XSComplexTypeDefinition) {
            System.out.println("XSComplexTypeDefinition for "+xsObject.getName());
            return new XSComplexTypeContainer(context, parent, (XSComplexTypeDefinition) xsObject);
        } else if (xsObject instanceof XSParticle) {
            System.out.println("XSParticle for "+xsObject.getName());
            return new XSParticleContainer(context, parent, (XSParticle) xsObject);
        } else if (xsObject instanceof XSModelGroup) {
            System.out.println("XSModelGroup for "+xsObject.getName());
            return new XSModelGroupContainer(context, parent, (XSModelGroup) xsObject);
        } else if (xsObject instanceof XSSimpleType) {
            System.out.println("XSSimpleType for "+xsObject.getName());
            return new XSSimpleTypeContainer(context, parent, (XSSimpleType) xsObject);
        } else if (xsObject instanceof XSAttributeDeclaration) {
            System.out.println("XSAttributeContainer for "+xsObject.getName());
            return new XSAttributeContainer(context, parent, (XSAttributeDeclaration) xsObject);
        } else if (xsObject instanceof XSWildcard) {
            //TODO skip by custom type
            return null;
        }
        throw new RuntimeException("Unknown element type: " + xsObject.getClass().getName());
    }
}
