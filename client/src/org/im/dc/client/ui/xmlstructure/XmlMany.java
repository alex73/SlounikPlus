package org.im.dc.client.ui.xmlstructure;

import java.awt.Component;
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

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTypeDefinition;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.ArticlePanelEdit;

/**
 * Container for equal elements depends on their min/max occurs.
 */
@SuppressWarnings("serial")
public class XmlMany extends JPanel {
    private static final Insets INSETS = new Insets(3, 3, 3, 3);

    private final ArticleUIContext context;
    private final XmlGroup parentPanel;
    protected final String tag;
    private final XSObject obj;
    private AnnotationInfo ann;
    private int minOccurs, maxOccurs;
    private final boolean writable;

    private XmlAdd add;

    public XmlMany(ArticleUIContext context, XmlGroup parentPanel, XSParticle obj, boolean parentWritable) {
        this(context, parentPanel, obj.getTerm(), ((XSElementDeclaration) obj.getTerm()).getAnnotation(),
                parentWritable);
        this.minOccurs = obj.getMinOccurs();
        this.maxOccurs = obj.getMaxOccurs() < 0 ? Integer.MAX_VALUE : obj.getMaxOccurs();

        initOccurs();
    }

    public XmlMany(ArticleUIContext context, XmlGroup parentPanel, XSAttributeUse obj, boolean parentWritable) {
        this(context, parentPanel, obj.getAttrDeclaration(), obj.getAttrDeclaration().getAnnotation(), parentWritable);
        minOccurs = obj.getRequired() ? 1 : 0;
        maxOccurs = 1;

        initOccurs();
    }

    protected XmlMany(ArticleUIContext context, XmlGroup parentPanel, XSObject obj, XSAnnotation annotation,
            boolean parentWritable) {
        this.context = context;
        this.parentPanel = parentPanel;
        this.tag = obj.getName();
        this.obj = obj;

        this.ann = new AnnotationInfo(annotation, obj.getName());

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
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addElement();
                context.fireChanged();
            }
        });
        add(add);

        writable = context.getWritable(parentWritable, ann);

        add.setVisible(writable);
    }

    private void initOccurs() {
        for (int i = 0; i < minOccurs; i++) {
            addElement();
        }
    }

    private void addElement() {
        JPanel p;

        XSTypeDefinition type;
        switch (obj.getType()) {
        case XSConstants.ELEMENT_DECLARATION:
            type = ((XSElementDeclaration) obj).getTypeDefinition();
            break;
        case XSConstants.ATTRIBUTE_DECLARATION:
            type = ((XSAttributeDeclaration) obj).getTypeDefinition();
            break;
        default:
            throw new RuntimeException("Unknown XSD type");
        }
        switch (ann.editType) {
        case DEFAULT:
            switch (type.getTypeCategory()) {
            case XSTypeDefinition.COMPLEX_TYPE:
                p = new XmlGroup(context, parentPanel, (XSElementDeclaration) obj, ann, writable);
                break;
            case XSTypeDefinition.SIMPLE_TYPE:
                XSSimpleType simpleType = (XSSimpleType) type;
                switch (simpleType.getPrimitiveKind()) {
                case XSSimpleType.PRIMITIVE_BOOLEAN:
                    p = new XmlEditBoolean(context, parentPanel, ann, writable);
                    break;
                case XSSimpleType.PRIMITIVE_DECIMAL:
                case XSSimpleType.PRIMITIVE_STRING:
                    p = new XmlEditText(context, parentPanel, ann, writable);
                    break;
                default:
                    throw new RuntimeException("Can't create editor for simple type: " + type.getName());
                }
                break;
            default:
                throw new RuntimeException("Unknown schema type: " + type.getTypeCategory());
            }
            break;
        case CHECK:
            p = new XmlEditCheck(context, parentPanel, ann, writable);
            break;
        case RADIO:
            p = new XmlEditRadio(context, parentPanel, ann, writable);
            break;
        case COMBO:
            p = new XmlEditCombo(context, parentPanel, ann, writable);
            break;
        case COMBO_EDITABLE:
            p = new XmlEditComboEditable(context, parentPanel, ann, writable);
            break;
        case ARTICLES_LIST:
            p = new XmlEditArticlesList(context, parentPanel, ann, writable);
            break;
        case CUSTOM:
            try {
                Class<? extends JPanel> editor = (Class<? extends JPanel>) Class.forName(ann.editDetails);
                Constructor<? extends JPanel> c = editor.getConstructor(XmlGroup.class, XmlGroup.class,
                        AnnotationInfo.class, ArticleEditController.class);
                p = c.newInstance(context, parentPanel, ann);
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
                    ArticlePanelEdit panelEdit = context.editController.panelEdit;
                    List<IXmlElement> allElements = getElements();
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
                        context.fireChanged();
                        revalidate();
                    });
                    panelEdit.cmMoveDown.addActionListener(l -> {
                        XmlMany.this.remove(idx);
                        XmlMany.this.add(p, idx + 1);
                        context.fireChanged();
                        revalidate();
                    });
                }
            }
        });
    }

    @Override
    public void revalidate() {
        if (add != null && writable) {
            add.setVisible(getComponentCount() - 1 < maxOccurs);
        }

        boolean canCloseSomeone = writable && (getComponentCount() - 1 > minOccurs);
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

        if (getComponent(pos) instanceof IXmlComplexElement) {
            IXmlComplexElement e = (IXmlComplexElement) getComponent(pos);
            e.insertData(rd);
        } else if (getComponent(pos) instanceof IXmlSimpleElement) {
            IXmlSimpleElement e = (IXmlSimpleElement) getComponent(pos);
            e.setData(rd.getElementText());
        } else {
            throw new RuntimeException("Unknown XSD type");
        }
    }

    public void setSimpleDataTo(String value) throws Exception {
        if (getComponentCount() <= 1) {
            addElement();
        }
        IXmlSimpleElement e = (IXmlSimpleElement) getComponent(0);
        e.setData(value);
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
            if (c instanceof IXmlComplexElement) {
                IXmlComplexElement e = (IXmlComplexElement) c;
                e.extractData(tag, wr);
            } else if (c instanceof IXmlSimpleElement) {
                IXmlSimpleElement e = (IXmlSimpleElement) c;
                String value = e.getData();
                if (value != null) {
                    switch (obj.getType()) {
                    case XSConstants.ELEMENT_DECLARATION:
                        wr.writeStartElement(tag);
                        wr.writeCharacters(value);
                        wr.writeEndElement();
                        break;
                    case XSConstants.ATTRIBUTE_DECLARATION:
                        wr.writeAttribute(tag, value);
                        break;
                    default:
                        throw new RuntimeException("Unknown XSD type");
                    }
                }
            } else {
                throw new RuntimeException("Unknown XSD type");
            }
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

    public String toString() {
        return this.getClass().getName() + " tag=" + tag;
    }
}
