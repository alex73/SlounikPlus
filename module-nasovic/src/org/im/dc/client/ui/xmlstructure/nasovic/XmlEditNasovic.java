package org.im.dc.client.ui.xmlstructure.nasovic;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.client.ui.xmlstructure.XmlMany;

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

        if (editController.isNew()) {
            SwingUtilities.invokeLater(() -> {
                p.setText("");
                p.paste();
                parse();
            });
        }
        return p;
    }

    void parse() {
        PaNumarach parsed = null;
        try {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setFontSize(attr, 20);
            ((StyledDocument) field.getDocument()).setCharacterAttributes(0, field.getDocument().getLength(), attr,
                    false);

            String text = field.getDocument().getText(0, field.getDocument().getLength());
            parsed = new PaNumarach(text);

            switch (parsed.numary.size()) {
            case 0: // пусты
                parsed.red();
                return;
            case 1:
                Numar n0 = parsed.numary.get(0);
                if (n0.skazy.size() < 2) {
                    parsed.red();
                    return;
                }
                Numar n1 = new Numar(text, n0.skazy.get(1).start, n0.end);
                while (n0.skazy.size() > 1) {
                    n0.skazy.remove(1);
                }
                n0.end = n0.skazy.get(0).end;
                parsed.numary.add(n1);
                break;
            case 2:
                if (parsed.numary.get(0).skazy.size() > 1) {
                    for (int j = 1; j < parsed.numary.get(0).skazy.size(); j++) {
                        parsed.numary.get(0).skazy.get(j).yellow();
                    }
                }
                break;
            }
            for (int i = 1; i < parsed.numary.size(); i++) {
                if (parsed.numary.get(i).skazy.isEmpty()) {
                    parsed.red();
                    return;
                }
                int firstItalic = parsed.numary.get(i).firstItalic();
                for (int j = firstItalic; j < parsed.numary.get(i).skazy.size(); j++) {
                    if (!parsed.numary.get(i).skazy.get(j).isItalic()) {
                        parsed.numary.get(i).skazy.get(j).yellow();
                    }
                }
                if (firstItalic != 1) {
                    for (int j = 0; j < parsed.numary.get(i).skazy.size(); j++) {
                        parsed.numary.get(i).skazy.get(j).yellow();
                    }
                }
            }

            XmlMany mZah = rootPanel.getManyPart("zah");
            prepareZahXml(parsed, mZah);

            XmlMany mTlum = rootPanel.getManyPart("tlum");
            prepareTlumXml(parsed, mTlum);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                parsed.red();
            } catch (Exception ex2) {
            }
        }
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        byte[] rtf = Base64.getDecoder().decode(rd.getElementText());
        field.setText("");
        field.getEditorKit().read(new ByteArrayInputStream(rtf), field.getDocument(), 0);
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws Exception {
        wr.writeStartElement(tag);

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        field.getEditorKit().write(o, field.getDocument(), 0, field.getDocument().getLength());
        o.flush();

        wr.writeCharacters(Base64.getEncoder().encodeToString(o.toByteArray()));
        wr.writeEndElement();
    }

    public class PaNumarach {
        List<Numar> numary = new ArrayList<>();

        public PaNumarach(String text) throws BadLocationException {
            int part = 1;
            int pos = 0;
            while (true) {
                String partPrefix = part + ")";
                System.out.println(partPrefix);
                int prevPos = pos;
                pos = text.indexOf(partPrefix, pos);
                if (pos < 0) {
                    numary.add(new Numar(text, prevPos, text.length()));
                    break;
                }
                numary.add(new Numar(text, prevPos, pos));
                pos += +partPrefix.length();
                part++;
            }
        }

        void red() {
            SimpleAttributeSet a = new SimpleAttributeSet();
            StyleConstants.setBackground(a, Color.RED);
            ((StyledDocument) field.getDocument()).setCharacterAttributes(0, field.getDocument().getLength(), a, false);
        }
    }

    public class Numar {
        private String fullText;
        int start, end;
        List<Skaz> skazy = new ArrayList<>();

        public Numar(String fullText, int start, int end) throws BadLocationException {
            this.fullText = fullText;
            this.start = start;
            this.end = end;
            int pos = start;
            do {
                int prevPos = pos;
                pos = nastupnySkaz(fullText, prevPos, end);
                skazy.add(new Skaz(fullText, prevPos, pos));
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

        int firstItalic() throws BadLocationException {
            for (int j = 0; j < skazy.size(); j++) {
                if (skazy.get(j).isItalic()) {
                    return j;
                }
            }
            return skazy.size();
        }

        String getText() {
            return fullText.substring(start, end).trim();
        }
    }

    public class Skaz {
        private String fullText;
        int start, end;

        public Skaz(String fullText, int start, int end) throws BadLocationException {
            this.fullText = fullText;
            this.start = start;
            this.end = end;

            System.out.println("    " + getText() + "  - italic=" + isItalic());
        }

        boolean fromBigLetter() {
            String tt = getText();
            if (tt.isEmpty()) {
                return false;
            }
            return Character.isUpperCase(tt.charAt(0));
        }

        boolean isItalic() throws BadLocationException {
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

        void yellow() {
            SimpleAttributeSet a = new SimpleAttributeSet();
            StyleConstants.setBackground(a, Color.YELLOW);
            ((StyledDocument) field.getDocument()).setCharacterAttributes(start, end - start, a, false);
        }

        String getAsSign() {
            String tt = getText();
            if ("Погов.".equals(tt) || "Посл.".equals(tt)) {
                return '(' + tt + ')';
            } else if ("(Погов.)".equals(tt) || "(Посл.)".equals(tt)) {
                return tt;
            } else {
                return null;
            }
        }

        String getText() {
            return fullText.substring(start, end).trim();
        }
    }

    void prepareZahXml(PaNumarach parsed, XmlMany mZah) throws Exception {
        StringWriter w = new StringWriter();
        XMLStreamWriter wr = ArticleEditController.WRITER_FACTORY.createXMLStreamWriter(w);
        wr.writeStartElement("zah");
        if (parsed.numary.size() > 0) {
            wr.writeCharacters(parsed.numary.get(0).getText());
        }
        wr.writeEndElement();
        wr.flush();

        byte[] xml = w.toString().getBytes("UTF-8");

        mZah.removeAllElements();
        mZah.insertDataTo(ArticleEditController.getReader(xml), 0);
    }

    void prepareTlumXml(PaNumarach parsed, XmlMany mTlum) throws Exception {
        mTlum.removeAllElements();

        for (int nn = 1; nn < parsed.numary.size(); nn++) {
            Numar n = parsed.numary.get(nn);
            StringWriter w = new StringWriter();
            XMLStreamWriter wr = ArticleEditController.WRITER_FACTORY.createXMLStreamWriter(w);
            wr.writeStartElement("tlum");
            if (n.skazy.size() > 0) {
                int tlFrom;
                if (n.skazy.get(0).fromBigLetter()) {
                    tlFrom = 0;
                } else {
                    tlFrom = 1;
                    wr.writeStartElement("gram");
                    wr.writeCharacters(n.skazy.get(0).getText());
                    wr.writeEndElement();
                }
                int tlTo = n.firstItalic();
                if (tlTo < 0) {
                    tlTo = n.skazy.size();
                }
                wr.writeStartElement("desc");
                for (int i = tlFrom; i < tlTo; i++) {
                    if (i > tlFrom) {
                        wr.writeCharacters(" ");
                    }
                    wr.writeCharacters(n.skazy.get(i).getText());
                }
                wr.writeEndElement();
                for (int i = tlTo; i < n.skazy.size(); i++) {
                    wr.writeStartElement("ex");
                    wr.writeCharacters(n.skazy.get(i).getText());
                    if (i < n.skazy.size() - 1) {
                        String sign = n.skazy.get(i + 1).getAsSign();
                        if (sign != null) {
                            wr.writeCharacters(" " + sign);
                            i++;
                        }
                    }
                    wr.writeEndElement();
                }
            }
            wr.writeEndElement();
            wr.flush();
            byte[] xml = w.toString().getBytes("UTF-8");
            System.out.println(w);

            mTlum.insertDataTo(ArticleEditController.getReader(xml), nn - 1);
        }
    }
}
