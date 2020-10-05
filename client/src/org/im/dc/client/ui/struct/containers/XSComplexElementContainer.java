package org.im.dc.client.ui.struct.containers;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.XSContainersFactory;
import org.im.dc.client.ui.struct.editors.XSEditCheck;
import org.w3c.dom.Element;

public class XSComplexElementContainer extends XSBaseContainer<XSComplexTypeDefinition> {
    protected XSElementDeclaration elem;
    private List<XSAttributeContainer> attributeChildren = new ArrayList<>();
    private XSParticleContainer particleChild;
    public final AnnotationInfo ann;
    private final JComponent panel;
    public IXSContainer customContainer;

    public XSComplexElementContainer(ArticleUIContext context, IXSContainer parentContainer, XSElementDeclaration elem,
            XSComplexTypeDefinition obj) {
        super(context, parentContainer, obj);
        this.elem = elem;
        ann = new AnnotationInfo(elem.getAnnotation());

        if (ann.customImpl != null) {
            // custom implementation
            try {
                Constructor<?> c = ann.customImpl.getConstructor(ArticleUIContext.class, IXSContainer.class,
                        XSElementDeclaration.class, AnnotationInfo.class);
                customContainer = (IXSContainer) c.newInstance(context, this, elem, ann);
            } catch (Exception ex) {
                throw new RuntimeException("Error create custom control from " + ann.customImpl.getName(), ex);
            }
            panel = customContainer.getUIComponent();
        } else if (ann.editType == AnnotationInfo.EDIT_TYPE.CHECK) {
            customContainer = new XSEditCheck(context, this, elem, ann);
            panel = customContainer.getUIComponent();
        } else {
            panel = new JPanel(new GridBagLayout());
            XSObjectList attrList = obj.getAttributeUses();
            for (int i = 0; i < attrList.getLength(); i++) {
                XSObject o = attrList.item(i);
                if (o instanceof XSAttributeUse) {
                    XSAttributeUse attr = (XSAttributeUse) o;
                    attributeChildren.add(XSContainersFactory.createUIAttribute(context, this,
                            attr.getAttrDeclaration(), attr.getRequired()));
                } else {
                    throw new RuntimeException("Unknown attribute declaration");
                }
            }
            if (obj.getParticle() != null) {
                particleChild = XSContainersFactory.createUIParticle(context, this, obj.getParticle());
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            for (IXSContainer child : attributeChildren) {
                panel.add(child.getUIComponent(), gbc);
                gbc.gridy++;
            }
            if (particleChild != null) {
                panel.add(particleChild.getUIComponent(), gbc);
            }
        }

        ann.applyColors(panel, parentContainer);
        updatePositionInList(-1,-1);
    }

    public void updatePositionInList(int position, int total) {
        String positionSuffix;
        if (position < 0) {
            positionSuffix = "";
        } else if (total == 1 && position == 0) {
            positionSuffix = "";
        } else {
            positionSuffix = " (" + (position + 1) + ")";
        }
        TitledBorder border;
        if (ann.borderColor != null) {
            border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ann.borderColor),
                    ann.text + positionSuffix);
            border.setTitleColor(ann.borderColor);
        } else {
            border = BorderFactory.createTitledBorder(ann.text + positionSuffix);
        }
        border.setTitleFont(new Font(border.getTitleFont().getName(), Font.BOLD, border.getTitleFont().getSize() + 2));
        panel.setBorder(border);
    }

    @Override
    public String getTag() {
        return elem.getName();
    }

    @Override
    public JComponent getUIComponent() {
        return panel;
    }

    @Override
    public boolean isWritable() {
        return context.getWritable(parentContainer, ann);
    }

    @Override
    public Collection<IXSContainer> children() {
        List<IXSContainer> result = new ArrayList<>();
        if (customContainer != null) {
            result.add(customContainer);
        } else {
            result.addAll(attributeChildren);
            if (particleChild != null) {
                result.add(particleChild);
            }
        }
        return result;
    }

    @Override
    public void insertData(Element node) throws Exception {
        if (!node.getNodeName().equals(elem.getName())) {
            throw new Exception("Wrong tag for reading");
        }
        if (customContainer != null) {
            customContainer.insertData(node);
        } else {
            for (XSAttributeContainer ac : attributeChildren) {
                ac.insertData(node);
            }
            if (particleChild != null) {
                particleChild.insertData(node);
            }
        }
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws Exception {
        if (customContainer != null) {
            customContainer.extractData(wr);
        } else {
            wr.writeStartElement(elem.getName());
            for (XSAttributeContainer ac : attributeChildren) {
                ac.extractData(wr);
            }
            if (particleChild != null) {
                particleChild.extractData(wr);
            }
            wr.writeEndElement();
        }
    }

    public String dump(String prefix) {
        String r = prefix + getClass().getSimpleName() + " " + ann.text + " [ \n";
        for (IXSContainer c : attributeChildren) {
            r += c.dump("  " + prefix);
        }
        if (customContainer != null) {
            customContainer.dump("  " + prefix);
        } else {
            if (particleChild != null) {
                r += particleChild.dump("  " + prefix);
            }
        }
        r += prefix + "]\n";
        return r;
    }
}
