package org.im.dc.client.ui.xmlstructure.tlum;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.alex73.corpus.paradigm.Paradigm;
import org.alex73.korpus.base.GrammarDB2;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;

@SuppressWarnings("serial")
public class XmlEditParadygmy extends XmlEditBase<JTextArea> {
    private List<VariantInfo> current = new ArrayList<>();

    public XmlEditParadygmy(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected JTextArea createField() {
        JTextArea f = new JTextArea();
        f.setLineWrap(false);
        f.setEditable(false);
        f.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        f.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SelectParadigmController(editController, XmlEditParadygmy.this, current);
            }
        });
        return f;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        Unmarshaller m = GrammarDB2.getContext().createUnmarshaller();
        while (true) {
            int n = rd.nextTag();
            if (n == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (n != XMLStreamConstants.START_ELEMENT || !rd.getLocalName().equals("var")) {
                throw new Exception("Expected 'var' tag");
            }
            if (rd.getAttributeCount() != 1 || !"index".equals(rd.getAttributeName(0).getLocalPart())) {
                throw new Exception("Expected 'index' attribute");
            }
            char variantIndex = rd.getAttributeValue(0).charAt(0);
            n = rd.nextTag();
            if (n != XMLStreamConstants.START_ELEMENT || !rd.getLocalName().equals("Paradigm")) {
                throw new Exception("Expected 'Paradigm' tag");
            }
            Paradigm p = m.unmarshal(rd, Paradigm.class).getValue();

            current.add(new VariantInfo(p, variantIndex));

            if (rd.getEventType() != XMLStreamConstants.END_ELEMENT || !rd.getLocalName().equals("var")) {
                throw new Exception("Expected 'var' tag end");
            }
        }
        showCurrent();
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws Exception {
        Marshaller m = GrammarDB2.getContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        wr.writeStartElement(tag);
        for (VariantInfo vi : current) {
            wr.writeStartElement("var");
            wr.writeAttribute("index", vi.variantIndex + "");
            m.marshal(vi.p, wr);
            wr.writeEndElement();
        }
        wr.writeEndElement();
    }

    public List<VariantInfo> getCurrent() {
        return current;
    }

    void setCurrent(Collection<VariantInfo> ps) {
        current.clear();
        current.addAll(ps);
        rootPanel.fireChanged();
        showCurrent();
    }

    void showCurrent() {
        StringBuilder s = new StringBuilder();
        for (VariantInfo c : current) {
            s.append(',').append(c);
        }
        field.setText(s.substring(1));
    }
}
