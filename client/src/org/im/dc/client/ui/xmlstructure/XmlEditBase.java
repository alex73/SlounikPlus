package org.im.dc.client.ui.xmlstructure;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class XmlEditBase<T extends JComponent> extends JPanel {
    protected final ArticleUIContext context;
    protected final AnnotationInfo ann;
    protected final boolean writable;
    public T field;
    private JButton closable;

    public XmlEditBase(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        this.context = context;
        this.ann = ann;
        writable = context.getWritable(parentWritable, ann);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        setLayout(new GridBagLayout());
        if (ann.bgColor != null) {
            setBackground(ann.bgColor);
        } else {
            setOpaque(false);
        }
        if (ann.fgColor != null) {
            setForeground(ann.fgColor);
        } else {
            setForeground(parentPanel.getForeground());
        }

        gbc.weightx = 0;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(ann.text + " : ");
        lbl.setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
        add(lbl, gbc);
        lbl.setForeground(getForeground());

        gbc.weightx = 1;
        gbc.gridx = 1;
        field = createField();
        add(field, gbc);

        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        closable = new JButton("×");
        closable.setForeground(lbl.getForeground());
        closable.setBorder(null);
        closable.setContentAreaFilled(false);
        add(closable, gbc);
        closable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Container parent = XmlEditBase.this.getParent();
                parent.remove(XmlEditBase.this);
                parent.revalidate();
                context.fireChanged();
            }
        });
    }

    protected abstract T createField();

    public void setClosableVisible(boolean visible) {
        this.closable.setVisible(visible);
    }

    public void setIndex(Integer index) {
    }

    public void displayed() {
    }
}
