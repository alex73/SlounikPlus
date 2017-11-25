package org.im.dc.client.ui.xmlstructure;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.im.dc.client.SchemaLoader;

@SuppressWarnings("serial")
public class XmlEditComboEditable extends XmlEditBase<JFilterComboBox> implements IXmlSimpleElement {
    public XmlEditComboEditable(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        super(context, parentPanel, ann,  parentWritable);
    }

    @Override
    protected JFilterComboBox createField() {
        JFilterComboBox fc = new JFilterComboBox(
                SchemaLoader.getSimpleTypeEnumeration(ann.editDetails, context.getArticleTypeId()));
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
