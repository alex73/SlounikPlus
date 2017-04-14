package org.im.dc.service.impl;

import java.util.ArrayList;
import java.util.List;

public class ValidationHelper {
    private List<String> links = new ArrayList<>();

    public void addLink(String link) {
        links.add(link);
    }

    public String[] getLinks() {
        return links.toArray(new String[0]);
    }
}
