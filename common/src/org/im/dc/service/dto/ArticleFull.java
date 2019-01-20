package org.im.dc.service.dto;

import java.util.Date;

public class ArticleFull {
    public int id;
    public String type;
    public String header;
    public byte[] xml;
    public String state;
    public String[] markers;
    public String[] assignedUsers;
    public byte[] notes;
    public Date lastUpdated;
    public String validationError;
}
