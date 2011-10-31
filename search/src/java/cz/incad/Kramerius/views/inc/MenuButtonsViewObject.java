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
package cz.incad.Kramerius.views.inc;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.shib.utils.ShibbolethUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MenuButtonsViewObject {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    KConfiguration kConfiguration;

    String[] getConfigredItems() {
        String[] langs = kConfiguration.getPropertyList("interface.languages");
        return langs;
    }
    
    public String getBaseURL() {
        String base  =  this.requestProvider.get().getRequestURL().toString();
        return base;
    }
    
    public String getQueryString() {
        HttpServletRequest request = this.requestProvider.get();
        if (request.getQueryString() != null) return request.getQueryString();
        else return "";
    }


    public String getShibbLogout() {
        HttpServletRequest req = this.requestProvider.get();
        if (ShibbolethUtils.isUnderShibbolethSession(req)) {
            String property = KConfiguration.getInstance().getProperty("security.shib.logout");
            return property;
        }
        return null;
    }
    
    
    public List<LanguageItem> getLanguageItems() {
        String[] items = getConfigredItems();
        List<LanguageItem> links = new ArrayList<LanguageItem>();
        for (int i = 0; i < items.length; i++) {
            String name = items[i];
            String link = i < items.length ? getBaseURL() + "?language="+ items[++i] + "&" + getQueryString() : "";
            LanguageItem itm = new LanguageItem(link, name);
            links.add(itm);
        }
        return links;
    }
    
    public static class LanguageItem {
        
        private String link;
        private String name;
        
        private LanguageItem(String link, String name) {
            super();
            this.link = link;
            this.name = name;
        }
        
        public String getLink() {
            return link;
        }
        
        public String getName() {
            return name;
        }
    }
}
