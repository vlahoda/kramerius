/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import net.sf.json.JSONObject;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.item.Decorator;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.XMLUtils;
import java.util.ArrayList;
import net.sf.json.JSONArray;

public class SolrTitleDecorate implements Decorator {

    public static final Logger LOGGER = Logger.getLogger(SolrTitleDecorate.class.getName());

    public static final String SOLR_TITLE_KEY = "SOLR_TITLE";

    @Inject
    SolrAccess solrAccess;

    @Override
    public String getKey() {
        return SOLR_TITLE_KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject) {
        try {
            String pid = jsonObject.getString("pid");
            Document solrDoc = this.solrAccess.getSolrDataDocument(pid);
            Element result = XMLUtils.findElement(solrDoc.getDocumentElement(), "result");
            if (result != null) {
                Element doc = XMLUtils.findElement(result, "doc");
                if (doc != null) {
                    String title = SOLRUtils.value(doc, "dc.title", String.class);
                    if (title != null) {
                        jsonObject.put("title", title);
                    }
                    String root_title = SOLRUtils.value(doc, "root_title", String.class);
                    if (root_title != null) {
                        jsonObject.put("root_title", root_title);
                    }
                    String root_model = SOLRUtils.value(doc, "root_model", String.class);
                    if (root_model != null) {
                        jsonObject.put("root_model", root_model);
                    }
                    String root_pid = SOLRUtils.value(doc, "root_pid", String.class);
                    if (root_pid != null) {
                        jsonObject.put("root_pid", root_pid);
                    }
                    // -> context nepatri sem
//                    List<String> pid_paths = SOLRUtils.array(doc, "pid_path", String.class);
//                    JSONArray ja = new JSONArray();
//                    for (String pid_path : pid_paths) {
//                        ja.add(pid_path);
//                    }
//                    jsonObject.put("pid_path", ja);
                    /// ??? -> context  nepatri sem
//                    List<String> model_paths = SOLRUtils.array(doc, "model_path", String.class);
//                    JSONArray jaa = new JSONArray();
//                    for (String model_path : model_paths) {
//                        jaa.add(model_path);
//                    }
//                    jsonObject.put("model_path", jaa);
                    
                    // ?? -> display options - asi vyhodit
//                    String viewable = SOLRUtils.value(doc, "viewable", Boolean.class);
//                    if (viewable != null) {
//                        jsonObject.put("viewable", viewable);
//                    }

                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public boolean applyOnContext(String context) {
        // TODO: jaky kontext dat ??
        return true;
    }

}
