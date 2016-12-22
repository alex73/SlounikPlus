package org.im.dc.client.ui.xmlstructure;

import javax.swing.JButton;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class XmlAdd extends JButton {
    public XmlAdd(AnnotationInfo ann) {
        super("+ " + ann.text);
        setBorder(null);
        if (ann.color != null) {
            setBackground(ann.color);
        } else {
            setOpaque(false);
            setContentAreaFilled(false);
        }
        //setBackground(Color.RED);
        //setContentAreaFilled(false);
        setHorizontalAlignment(SwingConstants.LEFT);
    }
}
