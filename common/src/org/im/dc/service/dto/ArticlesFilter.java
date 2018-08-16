package org.im.dc.service.dto;

import java.util.List;

public class ArticlesFilter {
    public String state;
    public String user;
    public String exactHeader;
    public String partHeader;
    public String partText;
    public List<Integer> ids;

    public String getLikeText() {
        if (partText == null) {
            return null;
        }
        return " " + partText.replace('*', '%') + " ";
    }

    public String getLikeHeader() {
        if (partHeader == null) {
            return null;
        }
        return partHeader.replace('*', '%');
    }
}
