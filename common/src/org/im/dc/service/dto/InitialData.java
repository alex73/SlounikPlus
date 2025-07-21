package org.im.dc.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class InitialData {
    public String configVersion;
    public String headerLocale;
    public String stress;
    public List<TypeInfo> articleTypes = new ArrayList<>();
    // map: key=user, value=roles
    public Map<String, String[]> allUsers;
    public Set<String> currentUserRoles;
    public List<String> states = new ArrayList<>(); // TODO make states per type and remove global list from xml
    public Set<String> currentUserPermissions = new TreeSet<>();
    public Map<String, byte[]> xsds;

    public static class TypeInfo {
        public String typeId;
        public String typeName;
        public String newArticleState;
        public boolean viewable;
        public Set<String> currentUserTypePermissions = new TreeSet<>();
        public Map<String, String[]> currentUserStateChanges = new TreeMap<>();
    }

    public TypeInfo getTypeInfo(String articleTypeId) {
        for (TypeInfo ti : articleTypes) {
            if (ti.typeId.equals(articleTypeId)) {
                return ti;
            }
        }
        return null;
    }
}
