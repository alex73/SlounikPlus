package org.im.dc.client.ui.struct.editors;

import javax.swing.JCheckBox;

import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

@SuppressWarnings("serial")
public class XSEditBoolean extends XSNamedControl<JCheckBox> implements IXSEdit {

    public XSEditBoolean(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        editor = new JCheckBox();
        editor.setOpaque(false);
        editor.addItemListener(c -> {
            context.fireChanged();
        });
        editor.setEnabled(context.getWritable(parentContainer.isWritable(), ann));
    }

    @Override
    public void setData(String data) throws Exception {
        editor.setSelected(Boolean.parseBoolean(data));
    }

    @Override
    public String getData() throws Exception {
        return editor.isSelected() ? "true" : "false";
    }
}
