package org.im.dc.client.ui.struct.editors;

import javax.swing.JComponent;

public interface IXSEdit {
    JComponent getUIComponent();

    String getData() throws Exception;

    void setData(String value) throws Exception;
}
