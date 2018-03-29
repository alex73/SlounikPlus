package org.im.dc.client.ui.struct.editors;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSElementDeclaration;
import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@SuppressWarnings("serial")
public class XSEditCheck extends XSNamedControl<JPanel> implements IXSContainer {
    private final XSElementDeclaration elem;

    public XSEditCheck(ArticleUIContext context, IXSContainer parentContainer, XSElementDeclaration elem,
            AnnotationInfo ann) {
        super(context, parentContainer, ann);
        this.elem = elem;
    }

    @Override
    public String getTag() {
        return elem.getName();
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    protected void initEditor() {
        FlowLayoutFullHeight layout = new FlowLayoutFullHeight();
        layout.setAlignment(FlowLayout.LEFT);
        editor = new JPanel(layout);
        editor.setOpaque(false);
        for (String v : SchemaLoader.getSimpleTypeEnumeration(ann.editDetails, context.getArticleTypeId())) {
            JCheckBox cb = new JCheckBox(v);
            cb.setOpaque(false);
            editor.add(cb);
            cb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    context.fireChanged();
                }
            });
        }
        editor.setEnabled(context.getWritable(parentContainer.isWritable(), ann));
    }

    @Override
    public Collection<IXSContainer> children() {
        return null;
    }

    @Override
    public void insertData(Element node) throws Exception {
        List<String> values = readValuesList(node);
        for (int i = 0; i < editor.getComponentCount(); i++) {
            JCheckBox cb = (JCheckBox) editor.getComponent(i);
            if (values.contains(cb.getText())) {
                cb.setSelected(true);
            }
        }
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(elem.getName());
        for (int i = 0; i < editor.getComponentCount(); i++) {
            JCheckBox cb = (JCheckBox) editor.getComponent(i);
            if (cb.isSelected()) {
                wr.writeStartElement("value");
                wr.writeCharacters(cb.getText());
                wr.writeEndElement();
            }
        }
        wr.writeEndElement();
    }

    List<String> readValuesList(XMLStreamReader rd) throws XMLStreamException {
        List<String> r = new ArrayList<>();
        while (true) {
            switch (rd.nextTag()) {
            case XMLStreamConstants.START_ELEMENT:

                if (!"value".equals(rd.getLocalName())) {
                    throw new RuntimeException("Wrong tag inside check list: " + rd.getLocalName());
                }
                r.add(rd.getElementText());
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (!elem.getName().equals(rd.getLocalName())) {
                    throw new RuntimeException("Wrong tag inside check list: " + rd.getLocalName());
                }
                return r;
            default:
                throw new RuntimeException();
            }
        }
    }

    List<String> readValuesList(Node node) throws XMLStreamException {
        List<String> r = new ArrayList<>();
        for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch.getNodeType() == Node.ELEMENT_NODE) {
                if (!"value".equals(ch.getNodeName())) {
                    throw new RuntimeException("Wrong tag inside check list: " + ch.getNodeName());
                }
                r.add(ch.getTextContent());
            }
        }
        return r;
    }

    @Override
    public String dump(String prefix) {
        return prefix + getClass().getSimpleName() + ": check\n";
    }
}
