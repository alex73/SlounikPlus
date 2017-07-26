package org.im.dc.client.ui.xmlstructure;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditComboEditable extends XmlEditBase<JFilterComboBox> {
    public XmlEditComboEditable(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JFilterComboBox createField() {
        JFilterComboBox fc = new JFilterComboBox(SchemaLoader.getSimpleTypeEnumeration(ann.editDetails));
        fc.setFont(rootPanel.getFont());
        fc.setSelectedItem("");
        fc.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rootPanel.fireChanged();
            }
        });
        return fc;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        field.setSelectedItem(rd.getElementText());
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        String text = (String) field.getSelectedItem();
        if (text != null) {
            wr.writeCharacters(text);
        }
        wr.writeEndElement();
    }
}
