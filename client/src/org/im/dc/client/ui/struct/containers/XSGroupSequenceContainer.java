package org.im.dc.client.ui.struct.containers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.XSContainersFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XSGroupSequenceContainer extends XSBaseContainer<XSModelGroup> {
    private List<IXSContainer> children = new ArrayList<>();
    private JPanel panel;

    public XSGroupSequenceContainer(ArticleUIContext context, IXSContainer parentContainer, XSModelGroup obj) {
        super(context, parentContainer, obj);

        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        XSObjectList sq = obj.getParticles();
        for (int i = 0; i < sq.getLength(); i++) {
            XSObject part = sq.item(i);
            children.add(XSContainersFactory.createUI(context, this, part));
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        for (IXSContainer child : children) {
            panel.add(child.getUIComponent(), gbc);
            gbc.gridy++;
        }
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

    public void insertData(Element node) throws Exception {
        for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            boolean found = false;
            for (IXSContainer ci : children) {
                if (ch.getNodeName().equals(ci.getTag())) {
                    ci.insertData((Element) ch);
                    found = true;
                }
            }
            if (!found) {
                throw new Exception("Child not found for tag " + node.getNodeName());
            }
        }
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws Exception {
        for (IXSContainer ci : children) {
            ci.extractData(wr);
        }
    }

    public String dump(String prefix) {
        String r = prefix + getClass().getSimpleName() + " " + obj.getName() + " group [ \n";
        for (IXSContainer c : children) {
            r += c.dump("  " + prefix);
        }
        r += prefix + "]\n";
        return r;
    }
}
