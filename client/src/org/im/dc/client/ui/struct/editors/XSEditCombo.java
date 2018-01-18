package org.im.dc.client.ui.struct.editors;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

@SuppressWarnings("serial")
public class XSEditCombo extends XSNamedControl<JComboBox<String>> implements IXSEdit {
    public XSEditCombo(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        Vector<String> values = new Vector<>(
                SchemaLoader.getSimpleTypeEnumeration(ann.editDetails, context.getArticleTypeId()));
        values.add(0, "");
        editor = new JComboBox<>(values);
        editor.setSelectedItem("");
        editor.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                context.fireChanged();
            }
        });
        editor.setEditable(false);
        editor.setEnabled(context.getWritable(parentContainer.isWritable(), ann));
    }

    @Override
    public void setData(String data) throws Exception {
        editor.setSelectedItem(data);
    }

    @Override
    public String getData() throws Exception {
        String value = (String) editor.getSelectedItem();
        return value.trim().isEmpty() ? null : value;
    }
}
