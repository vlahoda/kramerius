/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.service.ExportService;
import cz.incad.kramerius.service.PolicyService;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.XSLService;
import cz.incad.kramerius.service.impl.DeleteServiceImpl;
import cz.incad.kramerius.service.impl.ExportServiceImpl;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.service.impl.ReplicationServiceImpl;
import cz.incad.kramerius.service.impl.XSLServiceImpl;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeleteService.class).to(DeleteServiceImpl.class).in(Scopes.SINGLETON);
        bind(ExportService.class).to(ExportServiceImpl.class).in(Scopes.SINGLETON);
        bind(PolicyService.class).to(PolicyServiceImpl.class).in(Scopes.SINGLETON);
        bind(XSLService.class).to(XSLServiceImpl.class).in(Scopes.SINGLETON);

        bind(ReplicationService.class).to(ReplicationServiceImpl.class).in(Scopes.SINGLETON);
    }

}
