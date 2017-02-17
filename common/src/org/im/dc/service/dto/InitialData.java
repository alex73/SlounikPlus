package org.im.dc.service.dto;

import java.util.ArrayList;
import java.util.List;

public class InitialData {
    public byte[] articleSchema;
    public List<String> states = new ArrayList<>();
    public List<User> users = new ArrayList<>();

    public static class User {
        public String user;
        public String role;
    }
}
