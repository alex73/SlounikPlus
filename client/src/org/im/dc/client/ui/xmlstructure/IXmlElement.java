package org.im.dc.client.ui.xmlstructure;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public interface IXmlElement {
    void setClosableVisible(boolean visible);

    void insertData(XMLStreamReader rd) throws Exception;

    void extractData(String tag, XMLStreamWriter wr) throws Exception;

    void setIndex(Integer index);
}
