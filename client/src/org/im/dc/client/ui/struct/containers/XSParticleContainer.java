package org.im.dc.client.ui.struct.containers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSWildcard;
import org.im.dc.client.ui.ArticlePanelEdit;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.XSContainersFactory;

public class XSParticleContainer extends XSBaseContainer<XSParticle> {
    private static final Insets INSETS = new Insets(3, 0, 3, 0);
    private List<ChildInfo> children = new ArrayList<>();
    private JPanel panel;
    public final int minOccurs, maxOccurs;
    private JButton addButton;
    private boolean writable = true;

    public XSParticleContainer(ArticleUIContext context, IXSContainer parentContainer, XSParticle obj) {
        super(context, parentContainer, obj);

        panel = new JPanel(new VerticalListLayout(INSETS));
        panel.setOpaque(false);

        minOccurs = obj.getMinOccurs();
        maxOccurs = obj.getMaxOccursUnbounded() ? Integer.MAX_VALUE - 10 : obj.getMaxOccurs();

        if (minOccurs < maxOccurs) {
            XSTerm term = obj.getTerm();
            AnnotationInfo childAnn;
            switch (term.getType()) {
            case XSConstants.ELEMENT_DECLARATION:
                childAnn = new AnnotationInfo(((XSElementDeclaration) obj.getTerm()).getAnnotation());
                break;
            case XSConstants.WILDCARD:
                childAnn = new AnnotationInfo(((XSWildcard) obj.getTerm()).getAnnotation());
                break;
            default:
                throw new RuntimeException("Unknown child in the particle: " + term.getName());
            }
            createAddButton(childAnn);
        } else {
            createAddButton(null);
        }
        panel.add(addButton);

        for (int i = 0; i < minOccurs; i++) {
            addElement();
        }
        revalidate();
    }

    @Override
    public List<IXSContainer> children() {
        List<IXSContainer> result = new ArrayList<>();
        children.forEach(c -> result.add(c.element));
        return result;
    }

    public void removeAll() {
        for (ChildInfo ci : children) {
            panel.remove(ci.panel);
        }
        children.clear();
    }

    public IXSContainer addElement() {
        ChildInfo ci = new ChildInfo();
        ci.element = XSContainersFactory.createUI(context, this, obj.getTerm());

        ci.panel = new JPanel(new GridBagLayout());
        ci.panel.setOpaque(false);
        ci.closeButton = new JButton("×");
        ci.closeButton.setBorder(null);
        ci.closeButton.setContentAreaFilled(false);
        ci.closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                children.remove(ci);
                panel.remove(ci.panel);
                revalidate();
                context.fireChanged();
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        ci.panel.add(ci.element.getUIComponent(), gbc);

        gbc.weightx = 0;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        ci.panel.add(ci.closeButton, gbc);
        panel.add(ci.panel, panel.getComponentCount() - 1);

        children.add(ci);

        ci.panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            public void mouseClicked(MouseEvent e) {
                checkPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }

            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    ArticlePanelEdit panelEdit = context.editController.panelEdit;
                    int idx = indexOfPanel(ci.panel);
                    panelEdit.cmMoveUp.setEnabled(idx > 0);
                    panelEdit.cmMoveDown.setEnabled(idx < children.size() - 1);
                    panelEdit.contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    for (ActionListener l : panelEdit.cmMoveUp.getActionListeners()) {
                        panelEdit.cmMoveUp.removeActionListener(l);
                    }
                    for (ActionListener l : panelEdit.cmMoveDown.getActionListeners()) {
                        panelEdit.cmMoveDown.removeActionListener(l);
                    }
                    panelEdit.cmMoveUp.addActionListener(l -> {
                        children.remove(idx);
                        panel.remove(idx);
                        children.add(idx - 1, ci);
                        panel.add(ci.panel, idx - 1);
                        context.fireChanged();
                        revalidate();
                    });
                    panelEdit.cmMoveDown.addActionListener(l -> {
                        children.remove(idx);
                        panel.remove(idx);
                        children.add(idx + 1, ci);
                        panel.add(ci.panel, idx + 1);
                        context.fireChanged();
                        revalidate();
                    });
                }
            }
        });
        return ci.element;
    }

    @Override
    public String getTag() {
        return obj.getTerm().getName();
    }

    private int indexOfPanel(JPanel p) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).panel == p) {
                return i;
            }
        }
        return -1;
    }

    public void createAddButton(AnnotationInfo childAnn) {
        if (childAnn == null) {
            addButton = new JButton();
            addButton.setVisible(false);
            return;
        }
        addButton = new JButton("+ " + childAnn.text);
        addButton.setBorder(null);
        if (childAnn.bgColor != null) {
            addButton.setBackground(childAnn.bgColor);
        } else {
            addButton.setOpaque(false);
            addButton.setContentAreaFilled(false);
        }
        addButton.setHorizontalAlignment(SwingConstants.LEFT);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addElement();
                revalidate();
                context.fireChanged();
            }
        });
    }

    public void revalidate() {
        addButton.setVisible(writable && children.size() < maxOccurs);

        boolean canCloseSomeone = writable && (children.size() > minOccurs);
        for (ChildInfo ci : children) {
            ci.closeButton.setVisible(canCloseSomeone);
        }

        panel.revalidate();
    }

    @Override
    public JComponent getUIComponent() {
        return panel;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        if (obj.getTerm().getName() == null) {
            // non-named particle, should be 1:1
            if (minOccurs != 1 || maxOccurs != 1) {
                throw new Exception("Unsupported particle");
            }
            children.get(0).element.insertData(rd);
        } else {
            for (int pos = 0;; pos++) {
                if (rd.getLocalName().equals(obj.getTerm().getName())) {
                    if (children.size() <= pos) {
                        addElement();
                    }
                    children.get(pos).element.insertData(rd);
                } else {
                    break;
                }
                if (rd.nextTag() == XMLStreamConstants.END_ELEMENT) {
                    break;
                }
            }
        }
        revalidate();
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws Exception {
        for (ChildInfo ci : children) {
            ci.element.extractData(wr);
        }
    }

    public String dump(String prefix) {
        String r = prefix + getClass().getSimpleName() + " " + obj.getName() + " [" + obj.getMinOccurs() + ","
                + (obj.getMaxOccursUnbounded() ? ">" : obj.getMaxOccurs()) + "]" + " [ \n";
        for (ChildInfo c : children) {
            r += c.element.dump("  " + prefix);
        }
        r += prefix + "]\n";
        return r;
    }

    protected static class ChildInfo {
        IXSContainer element;
        JPanel panel;
        JButton closeButton;
    }
}
