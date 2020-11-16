package org.im.dc.client.ui.reports;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

import org.im.dc.client.WS;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.BaseController;
import org.im.dc.client.ui.MainController;
import org.im.dc.client.ui.ReportDialog;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleShort;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class ReportFieldValuesController extends BaseController<ReportDialog> {
    static JAXBContext CONTEXT;
    static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    static final Collator BE = Collator.getInstance(new Locale("be"));

    private Map<String, Map<String, List<ArticleShort>>> values = new TreeMap<>();

    private String articleTypeId;
    private final ReportFieldValues input;

    public ReportFieldValuesController(String articleTypeId) {
        super(new ReportDialog(MainController.instance.window, false), MainController.instance.window);
        this.articleTypeId = articleTypeId;
        input = new ReportFieldValues();
        window.output.addHyperlinkListener(hyperlinkListener);
        window.panelInput.add(input);
        window.btnStart.setVisible(false);

        window.setSize(1200, 700);
        setupCloseOnEscape();
        run();
        displayOnParent();
    }

    void run() {
        values.put("", new HashMap<>());
        new LongProcess() {
            AtomicInteger counter = new AtomicInteger();

            @Override
            protected void exec() throws Exception {
                List<ArticleFull> articles = WS.getToolsWebservice().getAllArticles(WS.header, articleTypeId);
                window.progress.setMaximum(articles.size());

                SAXParserFactory FACTORY = SAXParserFactory.newInstance();
                FACTORY.setNamespaceAware(true);
                ThreadLocal<SAXParser> T = ThreadLocal.withInitial(() -> {
                    try {
                        return FACTORY.newSAXParser();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                articles.parallelStream().forEach(a -> {
                    window.progress.setValue(counter.incrementAndGet());

                    try {
                        ArticleShort as = new ArticleShort();
                        as.id = a.id;
                        as.header = a.header;
                        T.get().parse(new ByteArrayInputStream(a.xml), new Parser(as));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }

            @Override
            protected void ok() {
                input.cbField.setModel(new DefaultComboBoxModel<>(new Vector<>(values.keySet())));
                input.cbField.addActionListener(e -> {
                    out(values.get(input.cbField.getSelectedItem()));
                });
            }

            @Override
            protected void error() {
                window.outputScrollPane.getViewport().removeAll();
            }
        };
    }

    HyperlinkListener hyperlinkListener = new HyperlinkListener() {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
                String desc = e.getDescription();
                if (desc == null || !desc.startsWith("#")) {
                    return;
                }
                int id = Integer.parseInt(desc.substring(1));
                new ArticleEditController(MainController.initialData.getTypeInfo(articleTypeId), id);
            }
        }
    };

    protected void out(Map<String, List<ArticleShort>> v) {
        Map<String, Object> data = new TreeMap<>();
        List<String> sortedValues = new ArrayList<>(v.keySet());
        Collections.sort(sortedValues);
        data.put("values", v);
        data.put("sortedValues", sortedValues);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setClassForTemplateLoading(this.getClass(), "/org/im/dc/client/ui/reports/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(true);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setNumberFormat("computer");
        DefaultObjectWrapperBuilder b = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28);
        b.setExposeFields(true);
        cfg.setObjectWrapper(b.build());

        String html;
        StringWriter o = new StringWriter();
        try {
            Template template = cfg.getTemplate("ReportFieldValues.template");
            template.process(data, o);
            html = o.toString();
        } catch (Exception ex) {
            html = ex.getMessage();
        }
        window.output.setText(html);
        window.output.setCaretPosition(0);
    }

    protected void addValue(String path, String text, ArticleShort a) {
        if (text.trim().isEmpty()) {
            return;
        }
        Map<String, List<ArticleShort>> va;
        List<ArticleShort> as;
        synchronized (values) {
            va = values.get(path);
            if (va == null) {
                va = new HashMap<>();
                values.put(path, va);
            }
        }
        synchronized (va) {
            as = va.get(text);
            if (as == null) {
                as = new ArrayList<>();
                va.put(text, as);
            }
        }
        synchronized (as) {
            as.add(a);
        }
    }

    class Parser extends DefaultHandler {
        ArticleShort a;
        StringBuilder path = new StringBuilder();
        StringBuilder text = new StringBuilder();

        public Parser(ArticleShort a) {
            this.a = a;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            text.setLength(0);
            path.append('/').append(localName);
            for (int i = 0; i < attributes.getLength(); i++) {
                String p = path + "@" + attributes.getLocalName(i);
                String v = attributes.getValue(i);
                addValue(p, v, a);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            addValue(path.toString(), text.toString(), a);
            text.setLength(0);
            int p = path.lastIndexOf("/");
            if (p < 0) {
                throw new RuntimeException("Error parse XML");
            }
            if (!path.substring(p + 1).equals(localName)) {
                throw new RuntimeException("Error parse XML");
            }
            path.setLength(p);
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            text.append(ch, start, length);
        }
    }
}
