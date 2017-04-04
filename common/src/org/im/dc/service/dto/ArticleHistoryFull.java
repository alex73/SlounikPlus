package org.im.dc.service.dto;

import java.util.Date;

public class ArticleHistoryFull {
    // калі быў зменены
    public Date when;
    // хто змяніў
    public String who;

    public byte[] oldXml, newXml;
}
