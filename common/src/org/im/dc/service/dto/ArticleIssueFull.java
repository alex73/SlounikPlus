package org.im.dc.service.dto;

import java.util.Date;

public class ArticleIssueFull {
    // калі быў зменены
    public Date when;
    // хто змяніў
    public String who;

    public String comment;

    public byte[] oldXml, newXml;

    public String fixer;
    public Date fixed;
}
