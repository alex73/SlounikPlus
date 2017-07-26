package org.im.dc.client.ui.xmlstructure;

import javax.swing.JCheckBox;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditBoolean extends XmlEditBase<JCheckBox> {
    public XmlEditBoolean(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JCheckBox createField() {
        JCheckBox f = new JCheckBox();
        f.setOpaque(false);
        f.addItemListener(c -> {
            rootPanel.fireChanged();
        });
        return f;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        field.setSelected(Boolean.parseBoolean(rd.getElementText()));
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        wr.writeCharacters(field.isSelected() ? "true" : "false");
        wr.writeEndElement();
    }
}
