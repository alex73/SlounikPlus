package org.im.dc.client.ui.xmlstructure;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class XmlEditArticlesList extends XmlEditBase<JFilterComboBox> implements IXmlSimpleElement {
    public XmlEditArticlesList(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann,
            boolean parentWritable) {
        super(context, parentPanel, ann, parentWritable);
    }

    @Override
    protected JFilterComboBox createField() {
        context.editController.requestRetrieveHeaders(ann.editDetails);
        JFilterComboBox fc = new JFilterComboBox(new ArrayList<>()) {
            @Override
            public void comboFilter(String enteredText) {
                if (getModel().getSize() == 0) {
                    List<String> headers = context.editController.getHeaders(ann.editDetails);
                    if (headers != null && !headers.isEmpty()) {
                        array = headers;
                    }
                }
                super.comboFilter(enteredText);
            }
        };
        fc.setFont(context.getFont());
        fc.setSelectedItem("");
        fc.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                context.fireChanged();
            }
        });
        fc.setEditable(writable);
        return fc;
    }

    @Override
    public void setData(String data) throws Exception {
        field.setSelectedItem(data);
    }

    @Override
    public String getData() throws Exception {
        return (String) field.getSelectedItem();
    }
}
