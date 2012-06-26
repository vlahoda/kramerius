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
package cz.incad.Kramerius.exts.menu.context.impl;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.exts.menu.context.ContextMenuItem;
import cz.incad.kramerius.service.ResourceBundleService;


public abstract class AbstractContextMenuItem implements ContextMenuItem {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractContextMenuItem.class.getName());
    
    @Inject
    protected ResourceBundleService resourceBundleService;

    @Inject
    protected Provider<Locale> localesProvider;
    
    @Inject
    protected Provider<HttpServletRequest> requestProvider;


    protected String renderContextMenuItem(String href, String labelKey) throws IOException {
        String label = this.resourceBundleService.getResourceBundle("labels", this.localesProvider.get()).getString(labelKey);
        StringTemplate template = new StringTemplate(
        "<li $if(multiselect)$ > $else$ class='no-multiple'> $endif$ <span class='ui-icon ui-icon-triangle-1-e  ' >item</span> <a title='$label$' href=\"$href$\">$label$</a></li>");
        template.setAttribute("multiselect", this.isMultipleSelectSupported());
        template.setAttribute("label", label);
        template.setAttribute("href",href);
        String rendered = template.toString();
        LOGGER.log(Level.INFO,"rendered item is '"+rendered+"'");
        return rendered;
    }
}
