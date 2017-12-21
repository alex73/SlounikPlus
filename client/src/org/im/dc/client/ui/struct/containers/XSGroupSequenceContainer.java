package org.im.dc.client.ui.struct.containers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.XSContainersFactory;

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

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        boolean found = false;
        for (IXSContainer ci : children) {
            if (rd.getLocalName().equals(ci.getTag())) {
                ci.insertData(rd);
                found = true;
            }
        }
        if (!found) {
            throw new Exception("Child not found for tag " + rd.getLocalName());
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