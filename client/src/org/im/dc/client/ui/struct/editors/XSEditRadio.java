package org.im.dc.client.ui.struct.editors;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

@SuppressWarnings("serial")
public class XSEditRadio extends XSNamedControl<JPanel> implements IXSEdit {

    public XSEditRadio(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        FlowLayoutFullHeight layout = new FlowLayoutFullHeight();
        layout.setAlignment(FlowLayout.LEFT);
        editor = new JPanel(layout);
        editor.setOpaque(false);
        ButtonGroup gr = new ButtonGroup();
        for (String v : SchemaLoader.getSimpleTypeEnumeration(ann.editDetails, context.getArticleTypeId())) {
            JRadioButton rb = new JRadioButton(v);
            rb.setFont(context.getFont());
            gr.add(rb);
            rb.setOpaque(false);
            editor.add(rb);
            rb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    context.fireChanged();
                }
            });
        }
        editor.setEnabled(context.getWritable(parentContainer.isWritable(), ann));
    }

    @Override
    public void setData(String data) throws Exception {
        for (int i = 0; i < editor.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) editor.getComponent(i);
            if (rb.getText().equals(data)) {
                rb.setSelected(true);
            }
        }
    }

    @Override
    public String getData() throws Exception {
        for (int i = 0; i < editor.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) editor.getComponent(i);
            if (rb.isSelected()) {
                return rb.getText();
            }
        }
        return null;
    }
}
