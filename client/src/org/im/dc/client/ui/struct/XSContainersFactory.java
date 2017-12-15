package org.im.dc.client.ui.struct;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTypeDefinition;
import org.im.dc.client.ui.struct.containers.XSAttributeContainer;
import org.im.dc.client.ui.struct.containers.XSComplexElementContainer;
import org.im.dc.client.ui.struct.containers.XSGroupChoiceContainer;
import org.im.dc.client.ui.struct.containers.XSGroupSequenceContainer;
import org.im.dc.client.ui.struct.containers.XSParticleContainer;
import org.im.dc.client.ui.struct.containers.XSSimpleElementContainer;

public class XSContainersFactory {
    public static XSAttributeContainer createUIAttribute(ArticleUIContext context, IXSContainer parent,
            XSAttributeDeclaration xsObject, boolean required) {
        return new XSAttributeContainer(context, parent, (XSAttributeDeclaration) xsObject, required);
    }

    public static XSParticleContainer createUIParticle(ArticleUIContext context, IXSContainer parent,
            XSObject xsObject) {
        return new XSParticleContainer(context, parent, (XSParticle) xsObject);
    }

    public static IXSContainer createUI(ArticleUIContext context, IXSContainer parent, XSObject xsObject) {
        if (xsObject instanceof XSElementDeclaration) {
            XSElementDeclaration xsElem = (XSElementDeclaration) xsObject;
            switch (xsElem.getTypeDefinition().getTypeCategory()) {
            case XSTypeDefinition.COMPLEX_TYPE:
                return new XSComplexElementContainer(context, parent, xsElem,
                        (XSComplexTypeDefinition) xsElem.getTypeDefinition());
            case XSTypeDefinition.SIMPLE_TYPE:
                return new XSSimpleElementContainer(context, parent, xsElem, (XSSimpleType) xsElem.getTypeDefinition());
            default:
                throw new RuntimeException("Unknown element type");
            }
        } else if (xsObject instanceof XSModelGroup) {
            XSModelGroup group = (XSModelGroup) xsObject;
            switch (group.getCompositor()) {
            case XSModelGroup.COMPOSITOR_SEQUENCE:
                return new XSGroupSequenceContainer(context, parent, group);
            case XSModelGroup.COMPOSITOR_CHOICE:
                return new XSGroupChoiceContainer(context, parent, group);
            default:
                throw new RuntimeException("Unknown compositor");
            }
        } else if (xsObject instanceof XSParticle) {
            return new XSParticleContainer(context, parent, (XSParticle) xsObject);
        }
        throw new RuntimeException("Unknown element type: " + xsObject.getClass().getName());
    }
}
