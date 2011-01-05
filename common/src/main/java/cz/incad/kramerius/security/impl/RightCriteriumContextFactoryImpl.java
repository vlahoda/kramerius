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
package cz.incad.kramerius.security.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.User;

public class RightCriteriumContextFactoryImpl implements RightCriteriumContextFactory {
    
    private FedoraAccess fedoraAccess;
    
    public RightCriteriumContextFactoryImpl() {
        super();
    }

    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }

    @Inject
    public void setFedoraAccess(@Named("securedFedoraAccess")FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    public static synchronized RightCriteriumContextFactoryImpl newFactory() {
        return new RightCriteriumContextFactoryImpl();
    }
    
    @Override
    public RightCriteriumContext create(String requestedUUID,  User user, String remoteHost, String remoteAddr) {
        RightCriteriumContext ctx = new RightParamEvaluatingContextImpl(requestedUUID, user, this.fedoraAccess, remoteHost, remoteAddr);
        return ctx;
    }
}
