package org.im.dc.client.ui.xmlstructure;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;

import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

@SuppressWarnings("serial")
public class XmlGroup extends JPanel implements IXmlElement {
    private final XmlGroup rootPanel;
    protected final ArticleEditController editController;
    private GridBagConstraints gbc = new GridBagConstraints();
    private JPanel gr;
    private JButton closable;
    private TitledBorder border;
    private String borderTitle;

    public XmlGroup(XmlGroup rootPanel, XmlGroup parentPanel, XSElementDeclaration obj, AnnotationInfo ann,
            ArticleEditController editController) {
        this.rootPanel = rootPanel != null ? rootPanel : this;
        this.editController = editController;
        setLayout(new GridBagLayout());
        setOpaque(false);

        gr = new JPanel();

        borderTitle = ann.text;

        if (ann.borderColor != null) {
            border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ann.borderColor), borderTitle);
            border.setTitleColor(ann.borderColor);
        } else {
            border = BorderFactory.createTitledBorder(borderTitle);
        }
        if (ann.bgColor != null) {
            gr.setBackground(ann.bgColor);
        } else {
            gr.setOpaque(false);
        }
        if (ann.fgColor != null) {
            setForeground(ann.fgColor);
        } else if (parentPanel != null) {
            setForeground(parentPanel.getForeground());
        }

        border.setTitleFont(new Font(border.getTitleFont().getName(), Font.BOLD, border.getTitleFont().getSize() + 2));
        gr.setBorder(border);
        gr.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(gr, gbc);

        p2(obj);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 0;
        closable = new JButton("×");
        closable.setForeground(getForeground());
        closable.setBorder(null);
        closable.setContentAreaFilled(false);
        closable.setVisible(false);
        add(closable, gbc);
        closable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Container parent = XmlGroup.this.getParent();
                parent.remove(XmlGroup.this);
                parent.revalidate();
                rootPanel.fireChanged();
            }
        });
    }

    void p2(XSElementDeclaration objElem) {
        XSTypeDefinition elemType = objElem.getTypeDefinition();
        if (elemType.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE) {
            throw new RuntimeException("Not a group");
        }
        XSComplexTypeDefinition elemTypeComplex = (XSComplexTypeDefinition) elemType;
        XSParticle pa1 = elemTypeComplex.getParticle();
        XSModelGroup group = (XSModelGroup) pa1.getTerm();
        XSObjectList list = group.getParticles();
        for (int i = 0; i < list.getLength(); i++) {
            XSParticle o = (XSParticle) list.item(i);
            XSElementDeclaration grElem = (XSElementDeclaration) o.getTerm();
            gr.add(new XmlMany(rootPanel, this, grElem, o.getMinOccurs(), o.getMaxOccurs()), gbc);
            gbc.gridy++;
        }
    }

    @Override
    public void setClosableVisible(boolean visible) {
        this.closable.setVisible(visible);
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        int groupIndex = 0;
        int indexInGroup = 0;
        while (rd.hasNext()) {
            int t = rd.nextTag();
            System.out.println(this + " " + rd.getLocalName());
            switch (t) {
            case XMLStreamConstants.START_ELEMENT:
                String tagName = rd.getLocalName();

                boolean done = false;
                while (groupIndex < gr.getComponentCount()) {
                    XmlMany many = (XmlMany) gr.getComponent(groupIndex);
                    if (many.tag.equals(tagName)) {
                        many.insertDataTo(rd, indexInGroup);
                        indexInGroup++;
                        done = true;
                        break;
                    } else {
                        groupIndex++;
                        indexInGroup = 0;
                    }
                }
                if (!done) {
                    throw new RuntimeException();
                }
                // if (rd.getEventType()==XMLStreamConstants.END_ELEMENT) {
                // return;
                // }
                break;
            case XMLStreamConstants.END_ELEMENT:
                return;
            default:
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void displayed() {
        for (Component c : gr.getComponents()) {
            XmlMany many = (XmlMany) c;
            many.displayed();
        }
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws Exception {
        wr.writeStartElement(tag);
        for (Component c : gr.getComponents()) {
            XmlMany many = (XmlMany) c;
            many.extractData(wr);
        }
        wr.writeEndElement();
    }

    public XmlMany getManyPart(String tag) {
        for (int i = 0; i < gr.getComponentCount(); i++) {
            XmlMany many = (XmlMany) gr.getComponent(i);
            if (many.tag.equals(tag)) {
                return many;
            }
        }
        return null;
    }

    @Override
    public void setIndex(Integer index) {
        if (borderTitle == null) {
            return;
        }
        if (index != null) {
            border.setTitle(borderTitle + " - " + index);
        } else {
            border.setTitle(borderTitle);
        }
    }

    /**
     * Сочыць за зменамі.
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void fireChanged() {
        for (ChangeListener cl : listenerList.getListeners(ChangeListener.class)) {
            cl.stateChanged(null);
        }
    }
}
