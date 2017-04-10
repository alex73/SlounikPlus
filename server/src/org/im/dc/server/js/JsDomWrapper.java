package org.im.dc.server.js;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class JsDomWrapper implements Map<String, Object> {
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private final Element el;

    public JsDomWrapper(byte[] xml) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        el = builder.parse(new ByteArrayInputStream(xml)).getDocumentElement();
    }

    private JsDomWrapper(Element el) {
        this.el = el;
    }

    @Override
    public int size() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isEmpty() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object get(Object key) {
        if ("textContent".equals(key)) {
            return el.getTextContent();
        }
        if (el.hasAttribute(key.toString())) {
            return el.getAttribute(key.toString());
        }

        List<JsDomWrapper> r = new ArrayList<>();
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(key)) {
                    r.add(new JsDomWrapper((Element) n));
                }
            }
        }
        return r.isEmpty() ? null : r;
    }

    @Override
    public Object put(String key, Object value) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Object remove(Object key) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void clear() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<String> keySet() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Collection<Object> values() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new RuntimeException("Not implemented");
    }
}
