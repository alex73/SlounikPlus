package org.im.dc.config;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.im.dc.gen.config.Change;
import org.im.dc.gen.config.CommonPermission;
import org.im.dc.gen.config.Config;
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
    public static boolean checkUser(Config config, String user, String pass) {
        for (User u : config.getUsers().getUser()) {
            if (u.getName().equals(user) && u.getPass().equals(pass)) {
                return true;
            }
        }
        return false;
    }

    public static String getUserRole(Config config, String user) {
        for (User u : config.getUsers().getUser()) {
            if (u.getName().equals(user)) {
                return u.getRole();
            }
        }
        return null;
    }

    public static Set<String> getUserPermissions(Config config, String user) {
        String userRole = getUserRole(config, user);
        for (Role r : config.getRoles().getRole()) {
            if (r.getName().equals(userRole)) {
                Set<String> result = new TreeSet<>();
                r.getPermission().forEach(cp -> result.add(cp.name()));
                return result;
            }
        }
        throw new RuntimeException("Roles not defined for user role: " + userRole);
    }

    public static Map<String, Set<String>> getUserPermissionsByType(Config config, String user) {
        String userRole = getUserRole(config, user);
        Map<String, Set<String>> result = new TreeMap<>();
        for (Type t : config.getTypes().getType()) {
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

    protected static Type getType(Config config, String typeId) {
        for (Type t : config.getTypes().getType()) {
            if (t.getId().equals(typeId)) {
                return t;
            }
        }
        throw new RuntimeException("Type not defined: " + typeId);
    }

    public static String getNewArticleState(Config config, String articleType) {
        Type type = getType(config, articleType);
        return type.getNewArticleState();
    }

    /*
     * public static String[] getUserNewArticleUsers(String user) { for (User u
     * : Config.getConfig().getUsers().getUser()) { if
     * (u.getName().equals(user)) { return u.getNewArticleUsers() != null ?
     * u.getNewArticleUsers().split(",") : null; } } return null; }
     */

    public static void userRequiresCommonPermission(Config config, String user, CommonPermission perm) {
        String userRole = getUserRole(config, user);
        for (Role r : config.getRoles().getRole()) {
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

    public static void userRequiresTypePermission(Config config, String user, String articleType, TypePermission perm) {
        String userRole = getUserRole(config, user);
        Type type = getType(config, articleType);
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

    /**
     * if assigned users defined - other users can't edit article. They can be not defined only for git.
     */
    private static boolean isUserAssigned(String user, String[] assignedUsers) {
        if (assignedUsers == null) {
            return false;
        }
        for (String au : assignedUsers) {
            if (user.equals(au)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canUserEditArticle(Config config, String user, String articleType, String articleState,
            String[] assignedUsers) {
        if (!isUserAssigned(user, assignedUsers)) {
            return false;
        }
        String userRole = getUserRole(config, user);
        Type t = getType(config, articleType);
        for (State st : t.getState()) {
            if (st.getName().equals(articleState) && st.getEditRoles() != null) {
                if (roleInRolesList(config, userRole, st.getEditRoles())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<String> canChangeStateTo(Config config, String user, String articleType, String articleState, String[] assignedUsers) {
        Set<String> result = new TreeSet<>();
        if (!isUserAssigned(user, assignedUsers)) {
            return result;
        }
        String userRole = getUserRole(config, user);
        Type t = getType(config, articleType);
        for (State st : t.getState()) {
            if (st.getName().equals(articleState) && st.getEditRoles() != null) {
                for (Change ch : st.getChange()) {
                    if (roleInRolesList(config, userRole, ch.getRoles())) {
                        result.add(ch.getTo());
                    }
                }
            }
        }
        return result;
    }

    public static boolean canUserChangeArticleState(Config config, String user, String articleType, String articleState,
            String newState, String[] assignedUsers) {
        String userRole = getUserRole(config, user);
        // check if common permission allow to force change status
        for (Role r : config.getRoles().getRole()) {
            if (r.getName().equals(userRole)) {
                for (CommonPermission p : r.getPermission()) {
                    if (CommonPermission.FORCE_STATE_CHANGE.equals(p)) {
                        return true;
                    }
                }
            }
        }

        // check if user allowed to change status for specific article
        if (!isUserAssigned(user, assignedUsers)) {
            return false;
        }
        Type t = getType(config, articleType);
        for (State st : t.getState()) {
            if (st.getName().equals(articleState)) {
                for (Change ch : st.getChange()) {
                    if (ch.getTo().equals(newState)) {
                        String toRoles = ch.getRoles();
                        if (roleInRolesList(config, userRole, toRoles)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean roleInRolesList(Config config, String role, String rolesList) {
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
