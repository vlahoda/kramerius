package cz.incad.kramerius.processes.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;


import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LRProcessDefinitionManagerImpl implements DefinitionManager {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(LRProcessDefinitionManagerImpl.class.getName());

	public static final String CONFIGURATION_FILE = Constants.WORKING_DIR+File.separator+"lp.xml";
	
	private KConfiguration configuration;
	private LRProcessManager processManager;

	
	
	@Inject
	public LRProcessDefinitionManagerImpl(KConfiguration configuration,
			LRProcessManager processManager) {
		super();
		this.configuration = configuration;
		this.processManager = processManager;
		this.load();
	}


	private HashMap<String, LRProcessDefinition> definitions = new HashMap<String, LRProcessDefinition>();


	@Override
	public LRProcessDefinition getLongRunningProcessDefinition(String id) {
		return definitions.get(id);
	}


	@Override
	public void load() {
		try {
			
			File conFile = new File(CONFIGURATION_FILE);
			if (!conFile.exists()) {
				StringTemplateGroup grp = new StringTemplateGroup("m");
				StringTemplate template = grp.getInstanceOf("cz/incad/kramerius/processes/res/lp");
				template.setAttribute("user_home", System.getProperties().getProperty("user.home"));
				String string = template.toString();
				conFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(conFile);
				try {
					IOUtils.copyStreams(new ByteArrayInputStream(string.getBytes(Charset.forName("UTF-8"))), fos);
				} finally {
					fos.close();
				}
			}
			
			LOGGER.info("Loading file from '"+CONFIGURATION_FILE+"'");
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document parsed = builder.parse(CONFIGURATION_FILE);
			NodeList childNodes = parsed.getDocumentElement().getChildNodes();
			for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
				Node item = childNodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					LRProcessDefinitionImpl def = new LRProcessDefinitionImpl(this.processManager, this.configuration);
					def.loadFromXml((Element) item);
					this.definitions.put(def.getId(), def);
				}
			}
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	
	@Override
	public List<LRProcessDefinition> getLongRunningProcessDefinitions() {
		return new ArrayList<LRProcessDefinition>(definitions.values());
	}


	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}


	public LRProcessManager getProcessManager() {
		return processManager;
	}


	public void setProcessManager(LRProcessManager processManager) {
		this.processManager = processManager;
	}

	public static void main(String[] args) {
		LRProcessDefinitionManagerImpl impl = new LRProcessDefinitionManagerImpl(KConfiguration.getKConfiguration(), null);
		
	}
}
