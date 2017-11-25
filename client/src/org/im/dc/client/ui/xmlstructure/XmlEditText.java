package org.im.dc.client.ui.xmlstructure;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class XmlEditText extends XmlEditBase<JTextArea> implements IXmlSimpleElement {
    public XmlEditText(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        super(context, parentPanel, ann,  parentWritable);
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
                context.fireChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                context.fireChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                context.fireChanged();
            }
        });
        f.setEditable(writable);
        return f;
    }

    @Override
    public void setData(String data) throws Exception {
        field.setText(data != null ? data : "");
    }

    @Override
    public String getData() throws Exception {
        String text = field.getText().trim();
        return text.isEmpty() ? null : text;
    }
}
