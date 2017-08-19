package org.im.dc.client.ui.xmlstructure;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.ArticlePanelEdit;

import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
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

        add = new XmlAdd(ann);
        if (ann.bgColor != null) {
            setBackground(ann.bgColor);
        } else {
            setOpaque(false);
        }
        if (ann.fgColor != null) {
            setForeground(ann.fgColor);
        } else {
            setForeground(parentPanel.getForeground());
        }
//        if (ann.fgColor != null) {
//            add.setForeground(ann.fgColor);
//        } else {
//            add.setForeground(parentPanel.getForeground());
//        }
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
            switch (obj.getTypeDefinition().getTypeCategory()) {
            case XSTypeDefinition.COMPLEX_TYPE:
                p = new XmlGroup(rootPanel, parentPanel, obj, ann, rootPanel.editController);
                break;
            case XSTypeDefinition.SIMPLE_TYPE:
                XSSimpleType type = (XSSimpleType) obj.getTypeDefinition();
                switch (type.getPrimitiveKind()) {
                case XSSimpleType.PRIMITIVE_BOOLEAN:
                    p = new XmlEditBoolean(rootPanel, parentPanel, ann, rootPanel.editController);
                    break;
                case XSSimpleType.PRIMITIVE_STRING:
                    p = new XmlEditText(rootPanel, parentPanel, ann, rootPanel.editController);
                    break;
                default:
                    throw new RuntimeException("Can't creaet editor for simple type: " + type.getName());
                }
                break;
            default:
                throw new RuntimeException("Unknown schema type: " + obj.getTypeDefinition().getTypeCategory());
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

        p.addMouseListener(new MouseAdapter() {
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
                    ArticlePanelEdit panelEdit = rootPanel.editController.panelEdit;
                    List<IXmlElement> allElements=getElements();
                    int idx = allElements.indexOf(p);
                    panelEdit.cmMoveUp.setEnabled(idx > 0);
                    panelEdit.cmMoveDown.setEnabled(idx < allElements.size() - 1);
                    panelEdit.contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    for (ActionListener l : panelEdit.cmMoveUp.getActionListeners()) {
                        panelEdit.cmMoveUp.removeActionListener(l);
                    }
                    for (ActionListener l : panelEdit.cmMoveDown.getActionListeners()) {
                        panelEdit.cmMoveDown.removeActionListener(l);
                    }
                    panelEdit.cmMoveUp.addActionListener(l -> {
                        XmlMany.this.remove(idx);
                        XmlMany.this.add(p, idx - 1);
                        rootPanel.fireChanged();
                        revalidate();
                    });
                    panelEdit.cmMoveDown.addActionListener(l -> {
                        XmlMany.this.remove(idx);
                        XmlMany.this.add(p, idx + 1);
                        rootPanel.fireChanged();
                        revalidate();
                    });
                }
            }
        });
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

    public void displayed() {
        for (Component c : getComponents()) {
            if (c == add) {
                continue;
            }
            IXmlElement e = (IXmlElement) c;
            e.displayed();
        }
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
