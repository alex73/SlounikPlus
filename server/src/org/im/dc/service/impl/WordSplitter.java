package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class WordSplitter {
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();
    private static final String LETTERS = "ёйцукенгшўзх'фывапролджэячсмітьбюЁЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮ-";

    private StringBuilder result = new StringBuilder(1024);
    private StringBuilder str = new StringBuilder(256);

    public String parse(byte[] xml) {
        result.append(' ');

        try {
            XMLStreamReader r = FACTORY.createXMLStreamReader(new ByteArrayInputStream(xml));
            while (r.hasNext()) {
                int eventType = r.next();
                switch (eventType) {
                case XMLEvent.CHARACTERS:
                    str.append(r.getText());
                    break;
                case XMLEvent.SPACE:
                    str.append(" ");
                    break;
                default:
                    if (str.length() > 0) {
                        process();
                        str.setLength(0);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        result.append(' ');

        return result.toString().toLowerCase();
    }

    private void process() {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '+') {
                continue;
            }
            if (LETTERS.indexOf(c) >= 0) {
                result.append(c);
            } else if (result.charAt(result.length() - 1) != ' ') {
                result.append(' ');
            }
        }
    }
}
