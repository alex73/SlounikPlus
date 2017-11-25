package org.im.dc.client.ui.xmlstructure;

public interface IXmlSimpleElement extends IXmlElement {

    void setData(String data) throws Exception;

    String getData() throws Exception;
}
