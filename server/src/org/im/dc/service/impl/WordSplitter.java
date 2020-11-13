package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class WordSplitter {
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final String stressChars;
    private StringBuilder result = new StringBuilder(1024);
    private StringBuilder str = new StringBuilder(256);

    public WordSplitter(String stressChars) {
        this.stressChars = stressChars;
    }

    public String parse(byte[] xml) {
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
                case XMLEvent.START_ELEMENT:
                    str.setLength(0);
                    str.append(' ');
                    for (int i = 0; i < r.getAttributeCount(); i++) {
                        str.append(r.getAttributeValue(i)).append(' ');
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    str.append(' ');
                    process();
                    str.setLength(0);
                    break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return result.toString().toLowerCase();
    }

    private void process() {
        if (str.toString().trim().isEmpty()) {
            return;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (stressChars.indexOf(c) >= 0) {
                continue;
            }
            result.append(c);
        }
        result.append('\n');
    }
}
