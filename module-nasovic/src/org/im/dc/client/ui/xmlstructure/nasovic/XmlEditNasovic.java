package org.im.dc.client.ui.xmlstructure.nasovic;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;

@SuppressWarnings("serial")
public class XmlEditNasovic extends XmlEditBase<JEditorPane> {

    public XmlEditNasovic(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JEditorPane createField() {
        JEditorPane p = new JEditorPane();
        p.setContentType("text/rtf");
        p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        p.getDocument().addDocumentListener(new DocumentListener() {
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
        /*
         * p.addKeyListener(new KeyAdapter() {
         * 
         * @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_F1) { p.setText("");
         * p.paste(); parse(); e.consume(); } } });
         */

        if (editController.isnew) {
            SwingUtilities.invokeLater(() -> {
                p.setText("");
                p.paste();
                parse();
            });
        }
        return p;
    }

    void parse() {
        try {
            SimpleAttributeSet a = new SimpleAttributeSet();
            StyleConstants.setFontSize(a, 20);
            ((StyledDocument) field.getDocument()).setCharacterAttributes(0, field.getDocument().getLength(), a, false);

            String text = field.getDocument().getText(0, field.getDocument().getLength());
            PaNumarach parsed = new PaNumarach(text);

            byte[] zah = prepareZahXml(parsed);

            editController.replacePart("/root/zah", zah);
            //rootPanel.replacePart("/root/tlum", rd);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void insertData(XMLStreamReader rd) throws XMLStreamException {
        field.setText(rd.getElementText());
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws XMLStreamException {
        wr.writeStartElement(tag);
        wr.writeCharacters(field.getText());
        wr.writeEndElement();
    }

    boolean isItalic(int start, int end) throws BadLocationException {
        String text = field.getDocument().getText(0, field.getDocument().getLength());
        boolean foundLetters = false;
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                Element el = ((StyledDocument) field.getDocument()).getCharacterElement(i);
                if (!StyleConstants.isItalic(el.getAttributes())) {
                    return false;
                }
                foundLetters = true;
            }
        }
        return foundLetters ? true : false;
    }

    public class PaNumarach {
        List<Numar> numary = new ArrayList<>();

        public PaNumarach(String text) throws BadLocationException {
            int part = 1;
            int pos = 0;
            while (true) {
                System.out.println((part - 1) + ")");
                int prevPos = pos;
                pos = text.indexOf(part + ")", pos);
                if (pos < 0) {
                    numary.add(new Numar(text, prevPos, text.length()));
                    break;
                }
                numary.add(new Numar(text, prevPos, pos));
                part++;
            }
        }
    }

    public class Numar {
        String fullText;
        List<String> skazy = new ArrayList<>();

        public Numar(String text, int start, int end) throws BadLocationException {
            int pos = start;
            do {
                int prevPos = pos;
                pos = nastupnySkaz(text, prevPos, end);
                System.out
                        .println("    " + text.substring(prevPos, pos).trim() + "  - italic=" + isItalic(prevPos, pos));
            } while (pos < end);
        }

        int nastupnySkaz(String text, int pos, int end) {
            for (int i = pos; i < end; i++) {
                if (text.charAt(i) == '.') {
                    for (int j = i + 1; j < end; j++) {
                        if (!Character.isSpaceChar(text.charAt(j))) {
                            char nextLetter = text.charAt(j);
                            if (Character.isLetter(nextLetter) && Character.isUpperCase(nextLetter)) {
                                return j;
                            }
                            break;
                        }
                    }
                }
            }
            return end;
        }
    }

    byte[] prepareZahXml(PaNumarach parsed) throws XMLStreamException, IOException {
        StringWriter w = new StringWriter();
        XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
        wr.writeStartElement("zah");
        if (parsed.numary.size() > 0) {
            wr.writeCharacters(parsed.numary.get(0).fullText);
        }
        wr.writeEndElement();
        wr.flush();
        return w.toString().getBytes("UTF-8");
    }

    byte[] prepareTlumXml(PaNumarach parsed) throws XMLStreamException, IOException {
        StringWriter w = new StringWriter();
        XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
        wr.writeStartElement("tlum");
        if (parsed.numary.size() > 0) {
            wr.writeCharacters(parsed.numary.get(0).fullText);
        }
        wr.writeEndElement();
        wr.flush();
        return w.toString().getBytes("UTF-8");
    }
}
