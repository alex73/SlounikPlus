package org.im.dc.client.ui.xmlstructure.tlum;

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.alex73.corpus.paradigm.Form;
import org.alex73.corpus.paradigm.Paradigm;
import org.alex73.corpus.paradigm.Variant;
import org.alex73.korpus.base.BelarusianTags;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;

@SuppressWarnings("serial")
public class XmlEditOneParadigm extends XmlEditBase<XmlEditOneParadigmPanel> {
    public XmlEditOneParadigm(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected XmlEditOneParadigmPanel createField() {
        XmlEditOneParadigmPanel r = new XmlEditOneParadigmPanel();

        XmlEditParadygmy paraList = (XmlEditParadygmy) rootPanel.getManyPart("paradyhmy").getElements().get(0);
        List<VariantInfo> current = paraList.getCurrent();
        Vector<String> list = new Vector<>();
        for (VariantInfo p : current) {
            list.add(p.toString());
        }
        r.cbPara.setModel(new DefaultComboBoxModel<String>(list));
        r.cbPara.addActionListener((e) -> {
            int idx = r.cbPara.getSelectedIndex();
            update(current.get(idx));
        });

        return r;
    }

    void update(VariantInfo s) {
        Paradigm p = SelectParadigmController.paradigmById.get(s.id);
        Variant v = p.getVariant().get(s.variantIndex - 'a');

        BelarusianTags tp = BelarusianTags.getInstance();
        for (Form f : v.getForm()) {
            char c = tp.getValueOfGroup(p.getTag(), "Склон");
            System.out.println(c);
        }

        field.table.setModel(new DefaultTableModel() {

        });
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws Exception {
        // TODO Auto-generated method stub

    }
}
