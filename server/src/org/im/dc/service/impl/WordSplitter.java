package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

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
                case XMLEvent.START_ELEMENT:
                    process();
                    for (int i = 0; i < r.getAttributeCount(); i++) {
                        str.append(r.getAttributeValue(i)).append(' ');
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    process();
                    break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return result.toString().toLowerCase();
    }

    static final Pattern SEPARATOR = Pattern.compile("[^\\p{IsAlphabetic}]");

    private void process() {
        String s = str.toString().trim();
        str.setLength(0);
        if (s.isEmpty()) {
            return;
        }
        for (char c : s.toCharArray()) {
            if (stressChars.indexOf(c) >= 0) {
                continue;
            }
            if (Character.isWhitespace(c)) {
                c = ' ';
            }
            str.append(c);
        }
        String[] words = SEPARATOR.split(str);
        for (String w : words) {
            result.append(w).append(' ');
        }
        str.setLength(0);
    }
}
