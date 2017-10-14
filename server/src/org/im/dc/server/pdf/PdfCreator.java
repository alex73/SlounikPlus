package org.im.dc.server.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.im.dc.server.Config;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.impl.HtmlOut;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

public class PdfCreator {

    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private Document doc;
    private PdfFont font;

    public PdfCreator() throws Exception {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        doc = new Document(pdf);

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("LiberationSerif-Regular.ttf")) {
            byte[] fontBytes = IOUtils.toByteArray(in);
            font = PdfFontFactory.createFont(fontBytes, "Identity-H", true);
        }
    }

    public void addHtmlArticle(String html) throws Exception {
        HtmlParse parser = new HtmlParse(html);
        doc.add(parser.parse());
    }

    public byte[] finish() {
        doc.close();
        return out.toByteArray();
    }

    public class HtmlParse {
        private final String html;
        private int pos;

        private List<String> tagStack = new ArrayList<>();
        private StringBuilder part = new StringBuilder();

        private Paragraph paragraph;

        public HtmlParse(String html) {
            this.html = html;
        }

        public Paragraph parse() throws Exception {
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(20.3f);
            paragraph.setFont(font);
            paragraph.setFontSize(10);

            for (pos = 0; pos < html.length(); pos++) {
                char c = html.charAt(pos);
                switch (c) {
                case '<':
                    // start tag
                    String tag = readTag();
                    flush();
                    switch (tag) {
                    case "b":
                        tagStack.add(tag);
                        break;
                    case "/b":
                        pop("b");
                        break;
                    case "i":
                        tagStack.add(tag);
                        break;
                    case "/i":
                        pop("i");
                        break;
                    default:
                        throw new ParseException("Unknown tag: " + tag, pos);
                    }
                    break;
                case '&':
                    String entity = readEntity();
                    switch (entity) {
                    case "lt":
                        append('<');
                        break;
                    case "gt":
                        append('>');
                        break;
                    case "amp":
                        append('&');
                        break;
                    default:
                        throw new ParseException("Unknown entity: " + entity, pos);
                    }
                    break;
                default:
                    append(c);
                }
            }
            return paragraph;
        }

        void append(char c) {
            part.append(c);
        }

        void flush() {
            if (part.length() == 0) {
                return;
            }
            Text element = new Text(part.toString());
            part.setLength(0);

            if (tagStack.contains("i")) {
                element.setItalic();
            }
            if (tagStack.contains("b")) {
                element.setBold();
            }
            paragraph.add(element);
        }

        void pop(String tag) throws Exception {
            if (tagStack.isEmpty()) {
                throw new Exception("Unsynchronized html tags");
            }
            String latest = tagStack.remove(tagStack.size() - 1);
            if (!latest.equals(tag)) {
                throw new Exception("Unsynchronized html tags");
            }
        }

        String readTag() throws ParseException {
            int start = pos;
            for (pos++; pos < html.length(); pos++) {
                char c = html.charAt(pos);
                if (c == '>') {
                    return html.substring(start + 1, pos);
                }
            }
            throw new ParseException("Tag was not closed", pos);
        }

        String readEntity() throws ParseException {
            int start = pos;
            for (pos++; pos < html.length(); pos++) {
                char c = html.charAt(pos);
                if (c == ';') {
                    return html.substring(start + 1, pos);
                }
            }
            throw new ParseException("Entity was not closed", pos);
        }
    }

    public static void main(String[] a) throws Exception {
        PdfCreator pdf = new PdfCreator();
        byte[] xml = FileUtils.readFileToByteArray(new File("../t.xml"));

        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", new String[] { "foo" }, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(xml), ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec(new File(Config.getConfigDir(), "../output.js").getAbsolutePath(), context);

        FileUtils.writeStringToFile(new File("../out.html"), out.toString(), "UTF-8");

        pdf.addHtmlArticle(out.toString());

        FileUtils.writeByteArrayToFile(new File("../out.pdf"), pdf.finish());
    }
}
