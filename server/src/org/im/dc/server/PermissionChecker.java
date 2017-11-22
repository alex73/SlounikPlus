package org.im.dc.server;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.Permission;
import org.im.dc.gen.config.Permissions;
import org.im.dc.gen.config.State;
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

    public static Map<String, Set<String>> getUserPermissions(String user) {
        String userRole = getUserRole(user);
        Map<String, Set<String>> result = new TreeMap<>();
        for (Permissions ps : Config.getConfig().getPermissions()) {
            if (ps.getRole().equals(userRole)) {
                Set<String> o = new TreeSet<>();
                result.put(ps.getType(), o);
                ps.getPermission().forEach(p -> o.add(p.name()));
            }
        }
        return result;
    }

    public static String getUserNewArticleState(String user) {
        for (User u : Config.getConfig().getUsers().getUser()) {
            if (u.getName().equals(user)) {
                return u.getNewArticleState();
            }
        }
        return null;
    }

    public static String[] getUserNewArticleUsers(String user) {
        for (User u : Config.getConfig().getUsers().getUser()) {
            if (u.getName().equals(user)) {
                return u.getNewArticleUsers() != null ? u.getNewArticleUsers().split(",") : null;
            }
        }
        return null;
    }

    public static void userRequiresPermission(String user, String articleType, Permission perm) {
        String userRole = getUserRole(user);
        for (Permissions ps : Config.getConfig().getPermissions()) {
            if (ps.getType().equals(articleType) && ps.getRole().equals(userRole)) {
                for (Permission p : ps.getPermission()) {
                    if (perm.equals(p)) {
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("No permission for this operation");
    }

    public static void canUserEditArticle(String user, String articleType, String articleState) {
        State state = Config.getStateByName(articleType, articleState);
        String editRoles = state.getEditRoles();
        if (editRoles == null) {
            throw new RuntimeException("No permission for edit");
        }
        if (!roleInRolesList(getUserRole(user), editRoles)) {
            throw new RuntimeException("No permission for edit");
        }
    }

    public static void canUserChangeArticleState(String user, String articleType, String articleState,
            String newState) {
        String userRole = getUserRole(user);
        State state = Config.getStateByName(articleType, articleState);
        for (Change ch : state.getChange()) {
            if (ch.getTo().equals(newState)) {
                String toRoles = ch.getRoles();
                if (roleInRolesList(userRole, toRoles)) {
                    return;
                }
            }
        }
        throw new RuntimeException("No permission for edit");
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
