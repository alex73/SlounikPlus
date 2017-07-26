package org.im.dc.client.ui.xmlstructure;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditRadio extends XmlEditBase<JPanel> {
    public XmlEditRadio(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JPanel createField() {
        JPanel field = new JPanel(new FlowLayout());
        field.setOpaque(false);
        ButtonGroup gr = new ButtonGroup();
        for (String v : SchemaLoader.getSimpleTypeEnumeration(ann.editDetails)) {
            JRadioButton rb = new JRadioButton(v);
            rb.setFont(rootPanel.getFont());
            gr.add(rb);
            rb.setOpaque(false);
            field.add(rb);
            rb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    rootPanel.fireChanged();
                }
            });
        }
        return field;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        String v = rd.getElementText();
        for (int i = 0; i < field.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) field.getComponent(i);
            if (v.equals(rb.getText())) {
                rb.setSelected(true);
            }
        }
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        for (int i = 0; i < field.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) field.getComponent(i);
            if (rb.isSelected()) {
                wr.writeCharacters(rb.getText());
            }
        }
        wr.writeEndElement();
    }
}
