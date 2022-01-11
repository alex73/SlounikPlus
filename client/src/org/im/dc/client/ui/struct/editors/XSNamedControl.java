package org.im.dc.client.ui.struct.editors;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

@SuppressWarnings("serial")
public abstract class XSNamedControl<T extends JComponent> extends JPanel {
    protected ArticleUIContext context;
    protected IXSContainer parentContainer;
    protected AnnotationInfo ann;
    protected T editor;

    public XSNamedControl(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(new BorderLayout());
        this.context = context;
        this.parentContainer = parentContainer;
        this.ann = ann;
        if (ann.bgColor != null) {
            setBackground(ann.bgColor);
        } else {
            setOpaque(false);
        }

        JLabel label = new JLabel(ann.text + ": ");
        if (ann.tooltip != null) {
            label.setToolTipText(ann.tooltip);
        }
        add(label, BorderLayout.WEST);

        initEditor();
        add(editor, BorderLayout.CENTER);
    }

    protected abstract void initEditor();

    public JComponent getUIComponent() {
        return this;
    }

    public T getEditor() {
        return editor;
    }

    public IXSContainer getParentContainer() {
        return parentContainer;
    }
}
