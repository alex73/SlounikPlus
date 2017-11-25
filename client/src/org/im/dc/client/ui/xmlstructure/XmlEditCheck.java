package org.im.dc.client.ui.xmlstructure;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;

@SuppressWarnings("serial")
public class XmlEditCheck extends XmlEditBase<JPanel> implements IXmlComplexElement {
    public XmlEditCheck(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        super(context, parentPanel, ann, parentWritable);
    }

    @Override
    protected JPanel createField() {
        FlowLayoutFullHeight layout = new FlowLayoutFullHeight();
        layout.setAlignment(FlowLayout.LEFT);
        JPanel field = new JPanel(layout);
        field.setOpaque(false);
        for (String v : SchemaLoader.getSimpleTypeEnumeration(ann.editDetails,
                context.editController.getArticleTypeId())) {
            JCheckBox cb = new JCheckBox(v);
            cb.setOpaque(false);
            cb.setFont(context.getFont());
            field.add(cb);
            cb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    context.fireChanged();
                }
            });
        }
        field.setEnabled(writable);
        return field;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws XMLStreamException {
        List<String> values = readValuesList(rd);
        for (int i = 0; i < field.getComponentCount(); i++) {
            JCheckBox cb = (JCheckBox) field.getComponent(i);
            if (values.contains(cb.getText())) {
                cb.setSelected(true);
            }
        }
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        for (int i = 0; i < field.getComponentCount(); i++) {
            JCheckBox cb = (JCheckBox) field.getComponent(i);
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
        while (rd.hasNext()) {
            int type = rd.next();
            switch (type) {
            case XMLStreamConstants.START_ELEMENT:
                if ("value".equals(rd.getLocalName())) {
                    r.add(rd.getElementText());
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                return r;
            default:
                throw new RuntimeException();
            }
        }
        return r;
    }
}
