package org.im.dc.client.ui.xmlstructure;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public interface IXmlComplexElement extends IXmlElement {

    void insertData(XMLStreamReader rd) throws Exception;

    void extractData(String tag, XMLStreamWriter wr) throws Exception;
}
