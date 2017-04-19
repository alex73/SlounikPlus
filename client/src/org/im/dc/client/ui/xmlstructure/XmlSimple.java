package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;

@SuppressWarnings("serial")
public class XmlSimple extends JPanel implements IXmlElement {
    private GridBagConstraints gbc = new GridBagConstraints();
    private JComponent field;
    private JButton closable;
    private final XmlGroup rootPanel;

    public XmlSimple(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann) {
        this.rootPanel = rootPanel;
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
        addField(ann);
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
    }

    private void addField(AnnotationInfo ann) {
        if (ann.type == null) {
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
            field = f;
        } else if (ann.type.startsWith("cbeditable/")) {
            String typeName = ann.type.substring("cbeditable/".length());
            JFilterComboBox f = new JFilterComboBox(SchemaLoader.getSimpleTypeEnumeration(typeName));
            f.setSelectedItem("");
          //  f.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            f.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    rootPanel.fireChanged();
                }
            });
            field = f;
        } else {
            throw new RuntimeException("Unknow article structure");
        }
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
                if (field instanceof JTextArea) {
                    JTextArea f = (JTextArea) field;
                    f.setText(f.getText() + rd.getText());
                } else if (field instanceof JComboBox) {
                    JComboBox<String> f = (JComboBox<String>) field;
                    f.setSelectedItem(rd.getText());
                } else {
                    throw new RuntimeException("Unknow article structure");
                }
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
        if (field instanceof JTextArea) {
            JTextArea f = (JTextArea) field;
            wr.writeCharacters(f.getText());
        } else if (field instanceof JComboBox) {
            JComboBox<String> f = (JComboBox<String>) field;
            wr.writeCharacters((String) f.getSelectedItem());
        } else {
            throw new RuntimeException("Unknow article structure");
        }

        wr.writeEndElement();
    }

    @Override
    public void setIndex(Integer index) {
    }
}
