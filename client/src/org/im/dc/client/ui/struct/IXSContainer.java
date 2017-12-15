package org.im.dc.client.ui.struct;

import java.util.Collection;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public interface IXSContainer {
    String getTag();

    String dump(String prefix);

    JComponent getUIComponent();

    IXSContainer getParentContainer();

    Collection<IXSContainer> children();

    boolean isWritable();

    void insertData(XMLStreamReader rd) throws Exception;

    void extractData(XMLStreamWriter wr) throws Exception;
}
