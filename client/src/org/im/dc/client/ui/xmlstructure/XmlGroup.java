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
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * Container for subelements.
 */
@SuppressWarnings("serial")
public class XmlGroup extends JPanel implements IXmlComplexElement {
    private final ArticleUIContext context;
    private GridBagConstraints gbc = new GridBagConstraints();
    private JPanel gr;
    private JButton closable;
    private TitledBorder border;
    private String borderTitle;
    private final boolean writable;

    public XmlGroup(ArticleUIContext context, XmlGroup parentPanel, XSElementDeclaration obj, AnnotationInfo ann, boolean parentWritable) {
        this.context = context;
        writable = context.getWritable(parentWritable, ann);
        setLayout(new GridBagLayout());

        gr = new JPanel();
        gr.setOpaque(false);

        borderTitle = ann.text;

        if (ann.borderColor != null) {
            border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ann.borderColor), borderTitle);
            border.setTitleColor(ann.borderColor);
        } else {
            border = BorderFactory.createTitledBorder(borderTitle);
        }
        if (ann.bgColor != null) {
            setBackground(ann.bgColor);
        } else {
            setOpaque(false);
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
                context.fireChanged();
            }
        });
    }

    public boolean isWritable() {
        return writable;
    }

    void p2(XSElementDeclaration objElem) {
        XSTypeDefinition elemType = objElem.getTypeDefinition();
        if (elemType.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE) {
            throw new RuntimeException("Not a group");
        }
        XSComplexTypeDefinition elemTypeComplex = (XSComplexTypeDefinition) elemType;
        XSObjectList attrList = elemTypeComplex.getAttributeUses();
        for (int i = 0; i < attrList.getLength(); i++) {
            XSObject o = attrList.item(i);
            if (o instanceof XSAttributeUse) {
                gr.add(new XmlMany(context, this, (XSAttributeUse) o, writable), gbc);
                gbc.gridy++;
            } else {
                throw new RuntimeException("Unknown attribute declaration");
            }
        }
        XSParticle pa1 = elemTypeComplex.getParticle();
        if (pa1 != null) {
            XSModelGroup group = (XSModelGroup) pa1.getTerm();
            XSObjectList list = group.getParticles();
            for (int i = 0; i < list.getLength(); i++) {
                gr.add(new XmlMany(context, this, (XSParticle) list.item(i),writable), gbc);
                gbc.gridy++;
            }
        }
    }

    @Override
    public void setClosableVisible(boolean visible) {
        this.closable.setVisible(visible && writable);
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        for (int i = 0; i < rd.getAttributeCount(); i++) {
            String attrName = rd.getAttributeLocalName(i);
            String attrValue = rd.getAttributeValue(i);
            boolean done = false;
            for (int groupIndex = 0; groupIndex < gr.getComponentCount(); groupIndex++) {
                XmlMany many = (XmlMany) gr.getComponent(groupIndex);
                if (many.tag.equals(attrName)) {
                    many.setSimpleDataTo(attrValue);
                    done = true;
                    break;
                }
            }
            if (!done) {
                throw new RuntimeException("Attribute not found");
            }
        }
        int groupIndex = 0;
        int indexInGroup = 0;
        while (rd.hasNext()) {
            int t = rd.nextTag();
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

    public String toString() {
        return this.getClass().getName();
    }
}
