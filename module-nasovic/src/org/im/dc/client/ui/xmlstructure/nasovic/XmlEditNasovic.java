package org.im.dc.client.ui.xmlstructure.nasovic;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.client.ui.xmlstructure.XmlMany;

@SuppressWarnings("serial")
public class XmlEditNasovic extends XmlEditBase<JEditorPane> {
    static final String ALLOWED_CHARS = "ЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮЁИЩЪѢйцукенгшўзхфывапролджэячсмітьбюёищъѣ´ ,.!?-;:\\(\\)–´";
    static final Pattern UPPER_FIRST = Pattern.compile(
            "([^.!?]\\s+)([ЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮЁИЩЪѢ][йцукенгшўзхфывапролджэячсмітьбюёищъѣ´\\-]+)([\\s,.!?;:\\(\\)–])");

    private String defaultFont;

    public XmlEditNasovic(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JEditorPane createField() {
        JEditorPane p = new JEditorPane();
        p.setContentType("text/rtf");
        defaultFont = p.getFont().getFontName();
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
                setFontSize();
                parse();
            });
        }
        return p;
    }

    void setFontSize() {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontSize(attr, 20);
        StyleConstants.setFontFamily(attr, defaultFont);
        ((StyledDocument) field.getDocument()).setCharacterAttributes(0, field.getDocument().getLength(), attr, false);
    }

    void parse() {
        PaNumarach parsed = null;
        try {
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

            text = field.getDocument().getText(0, field.getDocument().getLength());
            SimpleAttributeSet anacisk = new SimpleAttributeSet();
            StyleConstants.setBackground(anacisk, Color.CYAN);
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '´') {
                    ((StyledDocument) field.getDocument()).setCharacterAttributes(i, 1, anacisk, false);
                }
            }

            SimpleAttributeSet ablue = new SimpleAttributeSet();
            StyleConstants.setBackground(ablue, new Color(255, 128, 128));
            for (Numar n : parsed.numary) {
                for (int i = n.start; i < n.end; i++) {
                    if (ALLOWED_CHARS.indexOf(text.charAt(i)) < 0) {
                        ((StyledDocument) field.getDocument()).setCharacterAttributes(i, 1, ablue, false);
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
        byte[] data = Base64.getDecoder().decode(rd.getElementText());
        RTFSerialization.deserialize(field, new DataInputStream(new ByteArrayInputStream(data)));

        setFontSize();
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws Exception {
        wr.writeStartElement(tag);

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        RTFSerialization.serialize(field, new DataOutputStream(o));

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
                if (text.charAt(i) == '.' || text.charAt(i) == '?' || text.charAt(i) == '!') {
                    String endText = text.substring(pos, i + 1).toLowerCase();
                    if (endText.endsWith(" см.") || endText.endsWith(" слов.") || endText.endsWith(" приб.")) {
                        continue;
                    }
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
            StyleConstants.setBackground(a, new Color(255, 255, 128));
            ((StyledDocument) field.getDocument()).setCharacterAttributes(start, end - start, a, false);
        }

        String getAsSign() {
            String tt = getText();
            if ("Погов.".equals(tt) || "Посл.".equals(tt) || "Изъ пѣсни.".equals(tt)) {
                return '(' + tt + ')';
            } else if ("(Погов.)".equals(tt) || "(Посл.)".equals(tt) || "(Изъ пѣсни.)".equals(tt)) {
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
            String d = parsed.numary.get(0).getText();
            d = UPPER_FIRST.matcher(d).replaceAll("$1{$2}$3");
            d = d.replace("{См}", "См");
            d = d.replace("{Слов}", "Слов");
            d = d.replace("{Приб}", "Приб");
            d = d.replaceAll("\\{(Русс\\S+)\\}", "$1");
            d = d.replaceAll("\\{(Бѣлор\\S+)\\}", "$1");
            wr.writeCharacters(d);
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
                StringBuilder desc = new StringBuilder(100);
                for (int i = tlFrom; i < tlTo; i++) {
                    if (i > tlFrom) {
                        desc.append(" ");
                    }
                    desc.append(n.skazy.get(i).getText());
                }
                String d = UPPER_FIRST.matcher(desc).replaceAll("$1{$2}$3");
                d = d.replace("{См}", "См");
                d = d.replace("{Слов}", "Слов");
                d = d.replace("{Приб}", "Приб");
                d = d.replaceAll("\\{(Русс\\S+)\\}", "$1");
                d = d.replaceAll("\\{(Бѣлор\\S+)\\}", "$1");
                wr.writeCharacters(d);
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

            mTlum.insertDataTo(ArticleEditController.getReader(xml), nn - 1);
        }
    }
}
