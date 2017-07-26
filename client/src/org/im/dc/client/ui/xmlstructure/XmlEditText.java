package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditText extends XmlEditBase<JTextArea> {
    public XmlEditText(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JTextArea createField() {
        JTextArea f = new JTextArea();
        f.setLineWrap(true);
        f.setWrapStyleWord(true);
        f.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        f.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                rootPanel.fireChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                rootPanel.fireChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rootPanel.fireChanged();
            }
        });
        return f;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        field.setText(rd.getElementText());
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        wr.writeCharacters(field.getText());
        wr.writeEndElement();
    }
}
