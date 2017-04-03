package org.im.dc.client.ui.xmlstructure;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

@SuppressWarnings("serial")
public class XmlMany extends JPanel {
    private static final Insets INSETS = new Insets(3, 3, 3, 3);

    private final XmlGroup rootPanel, parentPanel;
    protected final String tag;
    private XSElementDeclaration obj;
    private AnnotationInfo ann;
    private int minOccurs, maxOccurs;

    private XmlAdd add;

    public XmlMany(XmlGroup rootPanel, XmlGroup parentPanel, XSElementDeclaration obj, int minOccurs, int maxOccurs) {
        this.rootPanel = rootPanel;
        this.parentPanel = parentPanel;
        this.tag = obj.getName();
        this.obj = obj;
        this.ann = new AnnotationInfo(obj.getAnnotation());
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs < 0 ? Integer.MAX_VALUE : maxOccurs;

        setLayout(new VerticalListLayout(INSETS));
        setOpaque(false);

        add = new XmlAdd(ann);
        if (ann.fgColor != null) {
            add.setForeground(ann.fgColor);
        } else {
            add.setForeground(parentPanel.getForeground());
        }
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addElement();
                rootPanel.fireChanged();
            }
        });
        add(add);

        for (int i = 0; i < this.minOccurs; i++) {
            addElement();
        }
    }

    private void addElement() {
        JPanel p;

        if (obj.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            p = new XmlGroup(rootPanel, parentPanel, obj, ann);
        } else {
            p = new XmlSimple(rootPanel, parentPanel, (XSSimpleTypeDefinition) obj.getTypeDefinition(), ann);
        }

        add(p, getComponentCount() - 1);

        revalidate();
    }

    @Override
    public void revalidate() {
        if (add != null) {
            add.setVisible(getComponentCount() - 1 < maxOccurs);
        }

        boolean canCloseSomeone = getComponentCount() - 1 > minOccurs;
        for (Component c : getComponents()) {
            if (c == add) {
                continue;
            }
            IXmlElement e = (IXmlElement) c;
            e.setClosableVisible(canCloseSomeone);
        }
        for (int i = 0; i < getComponentCount() - 1; i++) {
            IXmlElement e = (IXmlElement) getComponent(i);
            e.setIndex(getComponentCount() > 2 ? i + 1 : null);
        }

        super.revalidate();
    }

    public void insertDataTo(XMLStreamReader rd, int pos) throws XMLStreamException {
        if (pos >= getComponentCount() - 1) {
            addElement();
        }
        IXmlElement e = (IXmlElement) getComponent(pos);
        e.insertData(rd);
    }

    public void extractData(XMLStreamWriter wr) throws XMLStreamException {
        for (Component c : getComponents()) {
            if (c == add) {
                continue;
            }
            IXmlElement e = (IXmlElement) c;
            e.extractData(tag, wr);
        }
    }
}
