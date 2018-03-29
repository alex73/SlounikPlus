package org.im.dc.client.ui.struct;

import java.util.Collection;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

public interface IXSContainer {
    String getTag();

    String dump(String prefix);

    JComponent getUIComponent();

    IXSContainer getParentContainer();

    Collection<IXSContainer> children();

    boolean isWritable();

    void insertData(Element node) throws Exception;

    void extractData(XMLStreamWriter wr) throws Exception;
}
