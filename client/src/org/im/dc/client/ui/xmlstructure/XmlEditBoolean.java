package org.im.dc.client.ui.xmlstructure;

import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class XmlEditBoolean extends XmlEditBase<JCheckBox> implements IXmlSimpleElement {
    public XmlEditBoolean(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        super(context, parentPanel, ann, parentWritable);
    }

    @Override
    protected JCheckBox createField() {
        JCheckBox f = new JCheckBox();
        f.setOpaque(false);
        f.addItemListener(c -> {
            context.fireChanged();
        });
        f.setEnabled(writable);
        return f;
    }

    @Override
    public void setData(String data) throws Exception {
        field.setSelected(Boolean.parseBoolean(data));
    }

    @Override
    public String getData() throws Exception {
        return field.isSelected() ? "true" : "false";
    }
}
