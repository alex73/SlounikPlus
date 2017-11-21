package org.im.dc.client.ui.xmlstructure;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditComboDictionaryEditable extends XmlEditBase<JFilterComboBox> {
    public XmlEditComboDictionaryEditable(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JFilterComboBox createField() {
        /*Dictionaries.Dictionary dict = editController.dictionaries.dicts.get(ann.editDetails);
        if (dict == null) {
            dict = new Dictionaries.Dictionary();
        }
        JFilterComboBox fc = new JFilterComboBox(dict.values);
        fc.setFont(rootPanel.getFont());
        fc.setSelectedItem("");
        fc.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rootPanel.fireChanged();
            }
        });
        return fc;*/
        return null;
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
