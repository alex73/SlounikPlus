package org.im.dc.client.ui.xmlstructure;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditCheck extends XmlEditBase<JPanel> {
    public XmlEditCheck(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JPanel createField() {
        FlowLayoutFullHeight layout = new FlowLayoutFullHeight();
        layout.setAlignment(FlowLayout.LEFT);
        JPanel field = new JPanel(layout);
        field.setOpaque(false);
        for (String v : SchemaLoader.getSimpleTypeEnumeration(ann.editDetails)) {
            JCheckBox cb = new JCheckBox(v);
            cb.setOpaque(false);
            cb.setFont(rootPanel.getFont());
            field.add(cb);
            cb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    rootPanel.fireChanged();
                }
            });
        }
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
}
