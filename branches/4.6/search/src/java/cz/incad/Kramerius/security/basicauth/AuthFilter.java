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
/**
 * 
 */
package cz.incad.Kramerius.security.basicauth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.logging.Level;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.K4GuiceFilter;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.security.jaas.K4User;

/**
 * Supporting basic authetnication filter
 * @author pavels
 */
public class AuthFilter extends K4GuiceFilter{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AuthFilter.class.getName());
    
    private String realm = null;
    
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider = null;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) arg0;
            HttpServletResponse response = (HttpServletResponse) arg1;
            if (request.getUserPrincipal() == null) {
                String header = request.getHeader("Authorization");
                if (header!=null && header.trim().startsWith("Basic")) {
                    String uname = header.trim().substring("Basic".length()).trim();
                    byte[] decoded = Base64Coder.decode(uname.toCharArray());
                    String fname = new String(decoded, "UTF-8");
                    if (fname.contains(":")) {
                        String username = fname.substring(0, fname.indexOf(':'));
                        String password = fname.substring(fname.indexOf(':')+1);
                        HashMap<String,Object> user = K4LoginModule.findUser(connectionProvider.get(), username);
                        if (user != null) {
                            boolean checked = K4LoginModule.checkPswd(username, user.get("pswd").toString(), password.toCharArray());
                            if (checked) {
                                K4User principal = new K4User(username);
                                HttpServletRequest authenticated = BasicAuthenticatedHTTPServletProxy.newInstance(request, principal);
                                arg2.doFilter(authenticated, response);
                            } else {
                                sendError(response);
                            }
                        } else {
                            sendError(response);
                        }
                    } else {
                        sendError(response);
                    }
                } else {
                    sendError(response);
                }
            } else {
                // authenticated user - only forward
                arg2.doFilter(request, response);
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    public void sendError(HttpServletResponse response) throws IOException {
        response.setHeader( "WWW-Authenticate", "Basic realm=\"" + this.realm + "\"" );
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {
        super.init(conf);
        LOGGER.info("initializing auth filter...");
        this.realm = conf.getInitParameter("realm");
    }
}
