package org.im.dc.client.ui.xmlstructure;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;

import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
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

        switch (ann.editType) {
        case DEFAULT:
            if (obj.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                p = new XmlGroup(rootPanel, parentPanel, obj, ann, rootPanel.editController);
            } else {
                p = new XmlEditText(rootPanel, parentPanel, ann, rootPanel.editController);
            }
            break;
        case CHECK:
            p = new XmlEditCheck(rootPanel, parentPanel, ann, rootPanel.editController);
            break;
        case RADIO:
            p = new XmlEditRadio(rootPanel, parentPanel, ann, rootPanel.editController);
            break;
        case COMBO_EDITABLE:
            p = new XmlEditComboEditable(rootPanel, parentPanel, ann, rootPanel.editController);
            break;
        case CUSTOM:
            try {
                Class<? extends JPanel> editor = (Class<? extends JPanel>) Class.forName(ann.editDetails);
                Constructor<? extends JPanel> c = editor.getConstructor(XmlGroup.class, XmlGroup.class,
                        AnnotationInfo.class, ArticleEditController.class);
                p = c.newInstance(rootPanel, parentPanel, ann, rootPanel.editController);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            break;
        default:
            throw new RuntimeException("Unknown edit type");
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

    public void insertDataTo(XMLStreamReader rd, int pos) throws Exception {
        if (pos >= getComponentCount() - 1) {
            addElement();
        }
        IXmlElement e = (IXmlElement) getComponent(pos);
        e.insertData(rd);
    }

    public void extractData(XMLStreamWriter wr) throws Exception {
        for (Component c : getComponents()) {
            if (c == add) {
                continue;
            }
            IXmlElement e = (IXmlElement) c;
            e.extractData(tag, wr);
        }
    }

    public List<XmlGroup> getSubgroups() {
        List<XmlGroup> subgroups = new ArrayList<>();
        for (int i = 0; i < getComponentCount() - 1;) {
            if (getComponent(i) instanceof XmlGroup) {
                subgroups.add((XmlGroup) getComponent(i));
            }
        }
        return subgroups;
    }

    public List<IXmlElement> getElements() {
        List<IXmlElement> result = new ArrayList<>();
        for (int i = 0; i < getComponentCount() - 1; i++) {
            result.add((IXmlElement) getComponent(i));
        }
        return result;
    }

    public void removeAllElements() {
        for (int i = 0; i < getComponentCount() - 1;) {
            remove(i);
        }
    }
}
