package org.im.dc.client.ui.struct.containers;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.XSContainersFactory;
import org.im.dc.client.ui.struct.editors.FlowLayoutFullHeight;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XSGroupChoiceContainer extends XSBaseContainer<XSModelGroup> {
    private List<IXSContainer> children = new ArrayList<>();
    private JPanel panel;
    private JPanel pRadio;

    public XSGroupChoiceContainer(ArticleUIContext context, IXSContainer parentContainer, XSModelGroup obj) {
        super(context, parentContainer, obj);

        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        XSObjectList ch = obj.getParticles();
        for (int i = 0; i < ch.getLength(); i++) {
            XSObject part = ch.item(i);
            children.add(XSContainersFactory.createUI(context, this, part));
        }

        pRadio = new JPanel(new FlowLayoutFullHeight());
        pRadio.setOpaque(false);
        ButtonGroup bgRadio = new ButtonGroup();
        for (IXSContainer c : children) {
            if (!(c instanceof XSParticleContainer)) {
                throw new RuntimeException("Wrong child in the choice group");
            }
            XSParticleContainer pc = (XSParticleContainer) c;
            if (pc.minOccurs != 1 || pc.maxOccurs != 1) {
                throw new RuntimeException("Wrong child in the choice group");
            }
            IXSContainer c2 = pc.children().get(0);
            if (!(c2 instanceof XSComplexElementContainer)) {
                throw new RuntimeException("Wrong child in the choice group");
            }
            XSComplexElementContainer cc2 = (XSComplexElementContainer) c2;
            JRadioButton b = new JRadioButton(cc2.ann.text);
            b.setOpaque(false);
            b.setName(cc2.getTag());
            bgRadio.add(b);
            pRadio.add(b);
            b.addItemListener(rbChange);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(pRadio, gbc);
        gbc.gridy++;
        for (IXSContainer child : children) {
            Component c = child.getUIComponent();
            c.setVisible(false);
            panel.add(c, gbc);
            gbc.gridy++;
        }
    }

    ItemListener rbChange = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            int sel = getSelected();
            for (int i = 0; i < children.size(); i++) {
                children.get(i).getUIComponent().setVisible(sel == i);
            }
            context.fireChanged();
        }
    };

    private int getSelected() {
        if (children.size() != pRadio.getComponentCount()) {
            throw new RuntimeException("Wrong radio panel");
        }
        for (int i = 0; i < children.size(); i++) {
            boolean sel = ((JRadioButton) pRadio.getComponent(i)).isSelected();
            if (sel) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Collection<IXSContainer> children() {
        return children;
    }

    @Override
    public JComponent getUIComponent() {
        return panel;
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public void insertData(Element node) throws Exception {
        for (IXSContainer ci : children) {
            ci.getUIComponent().setVisible(false);
        }
        for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch.getNodeType()!=Node.ELEMENT_NODE) {
                continue;
            }
            for (int i = 0; i < children.size(); i++) {
                IXSContainer ci = children.get(i);
                if (ch.getNodeName().equals(ci.getTag())) {
                    ci.insertData((Element)ch);
                    ci.getUIComponent().setVisible(true);
                    ((JRadioButton) pRadio.getComponent(i)).setSelected(true);
                    break;
                }
            }
        }
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws Exception {
        int sel = getSelected();
        if (sel >= 0) {
            IXSContainer ci = children.get(sel);
            ci.extractData(wr);
        }
    }

    public String dump(String prefix) {
        String r = prefix + getClass().getSimpleName() + " " + obj.getName() + " choice group [ \n";
        for (IXSContainer c : children) {
            r += c.dump("  " + prefix);
        }
        r += prefix + "]\n";
        return r;
    }
}
