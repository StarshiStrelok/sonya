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
 * User profile registration status.
 * @author ss
 */
public enum RegistrationStatus {
    /** Profile created. */
    CREATED,
    /** Duplicate login. */
    DUPLICATE,
    /** Profile creation error. */
    ERROR,
    /** Allow only one profile restriction. */
    ALLOW_ONLY_ONE;
}
