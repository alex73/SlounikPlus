package org.im.dc.client.ui.struct.editors;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

@SuppressWarnings("serial")
public class XSEditComboFiltered extends XSNamedControl<JFilterComboBox> implements IXSEdit {
    public XSEditComboFiltered(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        editor = new JFilterComboBox(
                SchemaLoader.getSimpleTypeEnumeration(ann.editDetails, context.getArticleTypeId()));
        editor.setSelectedItem("");
        editor.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                context.fireChanged();
            }
        });
        editor.setEditable(true);
        editor.setEnabled(context.getWritable(parentContainer.isWritable(), ann));
    }

    @Override
    public void setData(String data) throws Exception {
        editor.setSelectedItem(data);
    }

    @Override
    public String getData() throws Exception {
        return (String) editor.getSelectedItem();
    }
}
