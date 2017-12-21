package org.im.dc.client.ui.struct.editors;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

@SuppressWarnings("serial")
public class XSEditText extends XSNamedControl<JTextArea> implements IXSEdit {

    public XSEditText(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        editor = new JTextArea();
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        Border outsideBorder = BorderFactory.createLineBorder(Color.BLACK);
        Border insideBorder = BorderFactory.createEmptyBorder(3, 0, 3, 0);
        editor.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
        editor.getDocument().addDocumentListener(new DocumentListener() {
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
        editor.setEditable(context.getWritable(parentContainer.isWritable(), ann));
    }

    @Override
    public void setData(String data) throws Exception {
        editor.setText(data != null ? data : "");
    }

    @Override
    public String getData() throws Exception {
        String text = editor.getText();// TODO trim and return null
        return text;
    }
}
