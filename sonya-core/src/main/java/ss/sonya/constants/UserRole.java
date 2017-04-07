/*
 * Copyright (C) 2017 ss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ss.sonya.constants;

/**
 * Profile user role.
 * @author SS
 */
public enum UserRole {
    /** Administrator role. */
    ROLE_ADMIN(new String[] {});
    /** Child roles. */
    private final String[] childRoles;
    /**
     * Constructor.
     * @param pChild - child roles.
     */
    UserRole(final String[] pChild) {
        childRoles = pChild;
    }
    /**
     * Get child roles.
     * @return - child roles.
     */
    public UserRole[] getChildRoles() {
        UserRole[] roles = new UserRole[childRoles.length];
        for (int i = 0; i < childRoles.length; i++) {
            roles[i] = getRoleByName(childRoles[i]);
        }
        return roles;
    }
    /**
     * Get user role by it name.
     * @param name - role name.
     * @return - constant or null.
     */
    public static UserRole getRoleByName(final String name) {
        for (UserRole ur : values()) {
            if (ur.name().equals(name)) {
                return ur;
            }
        }
        return null;
    }
}
