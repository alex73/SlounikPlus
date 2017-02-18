package org.im.dc.service.dto;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class InitialData {
    public byte[] articleSchema;
    public List<String> states;
    public List<String> allUsers;
    public String currentUserRole;
    public Set<String> currentUserPermissions = new TreeSet<>();
}
