package org.im.dc.client.ui.xmlstructure;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditRadio extends XmlEditBase<JPanel> implements IXmlSimpleElement {
    public XmlEditRadio(ArticleUIContext context,XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        super(context, parentPanel, ann,  parentWritable);
    }

    @Override
    protected JPanel createField() {
        FlowLayoutFullHeight layout = new FlowLayoutFullHeight();
        layout.setAlignment(FlowLayout.LEFT);
        JPanel field = new JPanel(layout);
        field.setOpaque(false);
        ButtonGroup gr = new ButtonGroup();
        for (String v : SchemaLoader.getSimpleTypeEnumeration(ann.editDetails,
                context.getArticleTypeId())) {
            JRadioButton rb = new JRadioButton(v);
            rb.setFont(context.getFont());
            gr.add(rb);
            rb.setOpaque(false);
            field.add(rb);
            rb.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    context.fireChanged();
                }
            });
        }
        field.setEnabled(writable);
        return field;
    }

    @Override
    public void setData(String data) throws Exception {
        for (int i = 0; i < field.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) field.getComponent(i);
            if (rb.getText().equals(data)) {
                rb.setSelected(true);
            }
        }
    }

    @Override
    public String getData() throws Exception {
        for (int i = 0; i < field.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) field.getComponent(i);
            if (rb.isSelected()) {
                return rb.getText();
            }
        }
        return null;
    }
}
