package org.im.dc.service.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InitialData {
    public String configVersion;
    public String headerLocale;
    public String stress;
    public Map<String, TypeInfo> articleTypes;
    // map: key=user, value=role
    public Map<String, String> allUsers;
    public String currentUserRole;
    public String newArticleState;
    public String[] newArticleUsers;

    public static class TypeInfo {
        public byte[] articleSchema;
        public List<String> states;
        public Set<String> currentUserPermissions = new TreeSet<>();
    }
}
