package org.im.dc.client.ui.xmlstructure;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;

@SuppressWarnings("serial")
public class XmlSimple extends JPanel implements IXmlElement {
    private GridBagConstraints gbc = new GridBagConstraints();
    private JTextField field;
    private JButton closable;

    public XmlSimple(XmlGroup rootPanel, XmlGroup parentPanel, XSSimpleTypeDefinition type, AnnotationInfo ann) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        setLayout(new GridBagLayout());
        setOpaque(false);

        gbc.weightx = 0;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(ann.text + " : ");
        lbl.setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
        add(lbl, gbc);
        if (ann.fgColor != null) {
            lbl.setForeground(ann.fgColor);
        } else {
            lbl.setForeground(parentPanel.getForeground());
        }

        gbc.weightx = 1;
        gbc.gridx = 1;
        field = new JTextField();
        add(field, gbc);

        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        closable = new JButton("×");
        closable.setForeground(lbl.getForeground());
        closable.setBorder(null);
        closable.setContentAreaFilled(false);
        add(closable, gbc);
        closable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Container parent = XmlSimple.this.getParent();
                parent.remove(XmlSimple.this);
                parent.revalidate();
                rootPanel.fireChanged();
            }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
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
    }

    @Override
    public void setClosableVisible(boolean visible) {
        this.closable.setVisible(visible);
    }

    @Override
    public void insertData(XMLStreamReader rd) throws XMLStreamException {
        while (rd.hasNext()) {
            int type = rd.next();
            switch (type) {
            case XMLStreamConstants.CHARACTERS:
                field.setText(field.getText() + rd.getText());
                break;
            case XMLStreamConstants.END_ELEMENT:
                return;
            default:
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        wr.writeCharacters(field.getText());
        wr.writeEndElement();
    }

    @Override
    public void setIndex(Integer index) {
    }
}
