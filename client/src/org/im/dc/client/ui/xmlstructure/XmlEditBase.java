package org.im.dc.client.ui.xmlstructure;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public abstract class XmlEditBase<T extends JComponent> extends JPanel implements IXmlElement {
    protected final XmlGroup rootPanel;
    protected final AnnotationInfo ann;
    protected final ArticleEditController editController;
    public T field;
    private JButton closable;

    public XmlEditBase(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        this.rootPanel = rootPanel;
        this.ann = ann;
        this.editController = editController;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        setLayout(new GridBagLayout());
        setOpaque(false);

        gbc.weightx = 0;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(ann.text + " : ");
        lbl.setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
        add(lbl, gbc);
        if (ann.fgColor != null) {
            lbl.setForeground(ann.fgColor);
        } else {
            lbl.setForeground(parentPanel.getForeground());
        }

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
                rootPanel.fireChanged();
            }
        });
    }

    protected abstract T createField();

    @Override
    public void setClosableVisible(boolean visible) {
        this.closable.setVisible(visible);
    }

    List<String> readValuesList(XMLStreamReader rd) throws XMLStreamException {
        List<String> r = new ArrayList<>();
        while (rd.hasNext()) {
            int type = rd.next();
            switch (type) {
            case XMLStreamConstants.START_ELEMENT:
                if ("value".equals(rd.getLocalName())) {
                    r.add(rd.getElementText());
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                return r;
            default:
                throw new RuntimeException();
            }
        }
        return r;
    }

    @Override
    public void setIndex(Integer index) {
    }
}
