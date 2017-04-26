package org.im.dc.client.ui.xmlstructure.tlum;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;

@SuppressWarnings("serial")
public class XmlEditParadygmy extends XmlEditBase<JTextArea> {
    public XmlEditParadygmy(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JTextArea createField() {
        JTextArea f = new JTextArea();
        f.setLineWrap(false);
        f.setEditable(false);
        f.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        f.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SelectParadigmController(editController, XmlEditParadygmy.this);
            }
        });
        return f;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws XMLStreamException {
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        wr.writeCharacters(field.getText());
        wr.writeEndElement();
    }
}
