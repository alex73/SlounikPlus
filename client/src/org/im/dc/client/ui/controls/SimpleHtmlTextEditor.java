package org.im.dc.client.ui.controls;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import org.apache.xerces.xs.XSElementDeclaration;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.editors.IXSEdit;
import org.im.dc.client.ui.struct.editors.XSNamedControl;

/**
 * Просты рэдактар, што дазваляе bold- і italic- тэгі.
 */
@SuppressWarnings("serial")
public class SimpleHtmlTextEditor extends XSNamedControl<JTextPane> implements IXSEdit {

    private static final SimpleAttributeSet BOLD = new SimpleAttributeSet();
    private static final SimpleAttributeSet ITALIC = new SimpleAttributeSet();
    private static final SimpleAttributeSet CLEAR = new SimpleAttributeSet();
    {
        StyleConstants.setBold(BOLD, true);
        StyleConstants.setItalic(ITALIC, true);
    }

    public SimpleHtmlTextEditor(ArticleUIContext context, IXSContainer parentContainer, XSElementDeclaration decl,
            AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    public SimpleHtmlTextEditor(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        editor = new JTextPane();
        editor.setEditorKit(new StyledEditorKit() {
            @Override
            public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
                StyledDocument d = (StyledDocument) doc;
                int bStart = -1, iStart = -1;
                StringBuilder str = new StringBuilder();
                List<int[]> bolds = new ArrayList<int[]>();
                List<int[]> italics = new ArrayList<int[]>();
                while (true) {
                    int c = in.read();
                    if (c < 0) {
                        break;
                    }
                    if (c == '〈') {
                        String tag = "";
                        while (true) {
                            int c2 = in.read();
                            if (c2 == '〉') {
                                break;
                            }
                            tag += (char) c2;
                        }
                        switch (tag) {
                        case "b":
                            bStart = str.length();
                            break;
                        case "/b":
                            int[] b = new int[2];
                            b[0] = bStart;
                            b[1] = str.length();
                            bStart = -1;
                            bolds.add(b);
                            break;
                        case "i":
                            iStart = str.length();
                            break;
                        case "/i":
                            int[] i = new int[2];
                            i[0] = iStart;
                            i[1] = str.length();
                            iStart = -1;
                            italics.add(i);
                            break;
                        }
                    } else {
                        str.append((char) c);
                    }
                }
                doc.insertString(pos, str.toString(), null);
                for (int[] b : bolds) {
                    d.setCharacterAttributes(pos + b[0], b[1] - b[0], BOLD, false);
                }
                for (int[] i : italics) {
                    d.setCharacterAttributes(pos + i[0], i[1] - i[0], ITALIC, false);
                }
            }

            @Override
            public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
                StyledDocument d = (StyledDocument) doc;
                for (int p = pos; p < len;) {
                    Element e = d.getCharacterElement(p);
                    if (StyleConstants.isItalic(e.getAttributes())) {
                        out.write("〈i〉");
                    }
                    if (StyleConstants.isBold(e.getAttributes())) {
                        out.write("〈b〉");
                    }
                    out.write(d.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset()));
                    if (StyleConstants.isBold(e.getAttributes())) {
                        out.write("〈/b〉");
                    }
                    if (StyleConstants.isItalic(e.getAttributes())) {
                        out.write("〈/i〉");
                    }
                    p = e.getEndOffset();
                }
            }
        });

        editor.setToolTipText("F2: тоўсты, F3: курсіў, F4: прыбраць");

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
        editor.setEditable(context.getWritable(parentContainer, ann));

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (!editor.isEditable()) {
                    return;
                }
                int b = editor.getSelectionStart();
                int e = editor.getSelectionEnd();
                switch (event.getKeyCode()) {
                case KeyEvent.VK_F2:
                    ((StyledDocument) editor.getDocument()).setCharacterAttributes(b, e - b, BOLD, false);
                    break;
                case KeyEvent.VK_F3:
                    ((StyledDocument) editor.getDocument()).setCharacterAttributes(b, e - b, ITALIC, false);
                    break;
                case KeyEvent.VK_F4:
                    ((StyledDocument) editor.getDocument()).setCharacterAttributes(b, e - b, CLEAR, true);
                    break;
                }
            }
        });
    }

    @Override
    public void setData(String data) throws Exception {
        editor.setText(data != null ? data : "");
    }

    @Override
    public String getData() throws Exception {
        String text = editor.getText().trim();
        return text.isEmpty() ? null : text;
    }
}
