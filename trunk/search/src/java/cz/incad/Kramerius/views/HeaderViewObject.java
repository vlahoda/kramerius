package cz.incad.Kramerius.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.ResourceBundleService;

/**
 * Objekt inicializujici js promenne v hlavicce 
 * @author pavels
 */
public class HeaderViewObject {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(HeaderViewObject.class.getName());
    
    @Inject
    Provider<HttpServletRequest> requestProvider;
    @Inject
    Provider<Locale> localeProvider;
    @Inject
    ResourceBundleService resourceBundleService;
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    public String getDictionary() {
        
        Map<String, String> resourceBundleMap = new HashMap<String, String>();
        try {
            Locale locale = this.localeProvider.get();
            ResourceBundle res = this.resourceBundleService.getResourceBundle("labels", locale);
            Set<String> keySet = res.keySet();
            for (String key : keySet) {
                String changedValue = res.getString(key);
                if (changedValue.contains("'")) {
                    changedValue = changedValue.replace("'", "\\'");
                }
                resourceBundleMap.put(key,changedValue);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        StringTemplateGroup grp = stGroup();
        StringTemplate inst = grp.getInstanceOf("dictionary");
        inst.setAttribute("resourceBundle", resourceBundleMap);
        return inst.toString();
    }

    private static StringTemplateGroup stGroup() {
        InputStream is = HeaderViewObject.class.getResourceAsStream("htmlHeaderJavascript.stg");
        StringTemplateGroup grp = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
        return grp;
    }
    
    
    public String getLevelsModelSelectionArray() {
        //StringTemplate template = new StringTemp
        StringTemplateGroup grp = stGroup();
        StringTemplate st = grp.getInstanceOf("levelModelsSelection");
        HashMap<String, String> model = new HashMap<String, String>(); {
            HttpServletRequest request = this.requestProvider.get();
            String pidPath = request.getParameter("pid_path");
            String path = request.getParameter("path");
            if ((pidPath != null) && (path !=null)) {
                String[] uuids = pidPath.split("/");
                String[] models = path.split("/");
                for (int i = 0; i < models.length; i++) {
                    String key = (i+1)+"_"+models[i];
                    model.put(key, uuids[i]);
                }
            }
        }
        st.setAttribute("data", model);
        return st.toString();
    }
}
