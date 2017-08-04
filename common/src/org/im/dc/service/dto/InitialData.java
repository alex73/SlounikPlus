package org.im.dc.service.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InitialData {
    public String configVersion;
    public byte[] articleSchema;
    public List<String> states;
    public Map<String, String> allUsers; // map: key=user, value=role
    public String currentUserRole;
    public Set<String> currentUserPermissions = new TreeSet<>();
    public String newArticleState;
    public String[] newArticleUsers;
}
