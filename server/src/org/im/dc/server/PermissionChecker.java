package org.im.dc.server;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.CommonPermission;
import org.im.dc.gen.config.Permissions;
import org.im.dc.gen.config.Role;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.Type;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.gen.config.User;

/**
 * Правярае дазволы на выкананьне асобных дзеяньняў.
 */
public class PermissionChecker {
    public static boolean checkUser(String user, String pass) {
        for (User u : Config.getConfig().getUsers().getUser()) {
            if (u.getName().equals(user) && u.getPass().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    public static String getUserRole(String user) {
        for (User u : Config.getConfig().getUsers().getUser()) {
            if (u.getName().equals(user)) {
                return u.getRole();
            }
        }
        return null;
    }

    public static Set<String> getUserPermissions(String user) {
        String userRole = getUserRole(user);
        for (Role r : Config.getConfig().getRoles().getRole()) {
            if (r.getName().equals(userRole)) {
                Set<String> result = new TreeSet<>();
                r.getPermission().forEach(cp -> result.add(cp.name()));
                return result;
            }
        }
        throw new RuntimeException("Roles not defined for user role: " + userRole);
    }

    public static Map<String, Set<String>> getUserPermissionsByType(String user) {
        String userRole = getUserRole(user);
        Map<String, Set<String>> result = new TreeMap<>();
        for (Type t : Config.getConfig().getTypes().getType()) {
            Set<String> o = new TreeSet<>();
            result.put(t.getId(), o);
            for (Permissions ps : t.getPermissions()) {
                if (ps.getRole().equals(userRole)) {
                    ps.getPermission().forEach(p -> o.add(p.name()));
                }
            }
        }
        return result;
    }

    protected static Type getType(String typeId) {
        for (Type t : Config.getConfig().getTypes().getType()) {
            if (t.getId().equals(typeId)) {
                return t;
            }
        }
        throw new RuntimeException("Type not defined: " + typeId);
    }

    public static String getNewArticleState(String articleType) {
        Type type = getType(articleType);
        return type.getNewArticleState();
    }

    /*
     * public static String[] getUserNewArticleUsers(String user) { for (User u :
     * Config.getConfig().getUsers().getUser()) { if (u.getName().equals(user)) { return u.getNewArticleUsers() != null
     * ? u.getNewArticleUsers().split(",") : null; } } return null; }
     */

    public static void userRequiresCommonPermission(String user, CommonPermission perm) {
        String userRole = getUserRole(user);
        for (Role r : Config.getConfig().getRoles().getRole()) {
            if (r.getName().equals(userRole)) {
                for (CommonPermission p : r.getPermission()) {
                    if (perm.equals(p)) {
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("No permission for this operation");
    }

    public static void userRequiresTypePermission(String user, String articleType, TypePermission perm) {
        String userRole = getUserRole(user);
        Type type = getType(articleType);
        for (Permissions ps : type.getPermissions()) {
            if (ps.getRole().equals(userRole)) {
                for (TypePermission p : ps.getPermission()) {
                    if (perm.equals(p)) {
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("No permission for this operation");
    }

    public static boolean canUserEditArticle(String user, String articleType, String articleState,
            String[] assignedUsers) {
        if (assignedUsers != null) {
            // if assigned users defined - other users can't edit article
            boolean found = false;
            for (String au : assignedUsers) {
                if (user.equals(au)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        String userRole = getUserRole(user);
        Type t = getType(articleType);
        for (State st : t.getState()) {
            if (st.getName().equals(articleState) && st.getEditRoles() != null) {
                if (roleInRolesList(userRole, st.getEditRoles())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<String> canChangeStateTo(String user, String articleType, String articleState) {
        String userRole = getUserRole(user);
        Type t = getType(articleType);
        Set<String> result = new TreeSet<>();
        for (State st : t.getState()) {
            if (st.getName().equals(articleState) && st.getEditRoles() != null) {
                for (Change ch : st.getChange()) {
                    if (PermissionChecker.roleInRolesList(userRole, ch.getRoles())) {
                        result.add(ch.getTo());
                    }
                }
            }
        }
        return result;
    }

    public static boolean canUserChangeArticleState(String user, String articleType, String articleState,
            String newState) {
        String userRole = getUserRole(user);
        Type t = getType(articleType);
        for (State st : t.getState()) {
            if (st.getName().equals(articleState)) {
                for (Change ch : st.getChange()) {
                    if (ch.getTo().equals(newState)) {
                        String toRoles = ch.getRoles();
                        if (roleInRolesList(userRole, toRoles)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean roleInRolesList(String role, String rolesList) {
        if (rolesList == null) {
            return false;
        }
        for (String r : rolesList.split(",")) {
            if (r.trim().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
