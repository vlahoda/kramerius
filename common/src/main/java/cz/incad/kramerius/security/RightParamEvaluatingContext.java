/*
 * Copyright (C) 2010 Pavel Stastny
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;

/**
 * Implementation of this interface holds information 
 * necessary for interpreting rule (current uuid, current user, fed 
 * @author pavels
 */
public interface RightParamEvaluatingContext {

    /**
     * UUID of the object
     * @return
     */
    public String getUUID();
    
    /**
     * Returns path from leaf to root tree
     * @return
     */
    public String[] getPathOfUUIDs();
    
    /**
     * Current logged user
     * @return
     * @see AbstractUser
     */
    public AbstractUser getUser();
    
    /**
     * Fedora access
     * @return
     */
    public FedoraAccess getFedoraAccess();
    
}
