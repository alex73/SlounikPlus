package org.im.dc.client.ui.xmlstructure;

import javax.swing.JButton;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class XmlAdd extends JButton {
    public XmlAdd(AnnotationInfo ann) {
        super("+ " + ann.text);
        setBorder(null);
        if (ann.bgColor != null) {
            setBackground(ann.bgColor);
        } else {
            setOpaque(false);
            setContentAreaFilled(false);
        }
        setHorizontalAlignment(SwingConstants.LEFT);
    }
}
