package cz.incad.Kramerius.backend.impl;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.kramerius.utils.RESTHelper.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class FedoraAccessImpl implements FedoraAccess {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(FedoraAccessImpl.class.getName());
	
	private final KConfiguration configuration;
	
	@Inject
	public FedoraAccessImpl(KConfiguration configuration) {
		super();
		this.configuration = configuration;
	}

	@Override
	public List<Element> getPages(String uuid, boolean deep) throws IOException {
		Document relsExt = getRelsExt(uuid);
		return getPages(uuid, relsExt.getDocumentElement());
	}
	
	@Override
	public Document getRelsExt(String uuid) throws IOException {
		String relsExtUrl = relsExtUrl(KConfiguration.getKConfiguration(), uuid);
		LOGGER.info("Reading rels ext +"+relsExtUrl);
		InputStream docStream = RESTHelper.inputStream(relsExtUrl, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
		try {
			return XMLUtils.parseDocument(docStream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
			throw new IOException(e);
		}
	}

	
	@Override
	public Document getBiblioMods(String uuid) throws IOException {
		String biblioModsUrl = biblioMods(KConfiguration.getKConfiguration(), uuid);
		LOGGER.info("Reading bibliomods +"+biblioModsUrl);
		InputStream docStream = RESTHelper.inputStream(biblioModsUrl, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
		try {
			return XMLUtils.parseDocument(docStream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}

	@Override
	public Document getDC(String uuid) throws IOException {
		String dcUrl = dc(KConfiguration.getKConfiguration(), uuid);
		LOGGER.info("Reading dc +"+dcUrl);
		InputStream docStream = RESTHelper.inputStream(dcUrl, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
		try {
			return XMLUtils.parseDocument(docStream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}


	
	@Override
	public void processRelsExt(Document relsExtDocument, RelsExtHandler handler) throws IOException{
		try {
			Stack<Element> processingStack = new Stack<Element>();
			processingStack.push(relsExtDocument.getDocumentElement());
			while(!processingStack.isEmpty()) {
				Element topElem = processingStack.pop();
				String namespaceURI = topElem.getNamespaceURI();
				if (namespaceURI.equals(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI)) {
					String nodeName = topElem.getLocalName();
					FedoraRelationship relation = FedoraRelationship.valueOf(nodeName);
					if (relation != null) {
						if (handler.accept(relation)) {
							handler.handle(topElem, relation, processingStack);
							// deep
							String attVal = topElem.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
							PIDParser pidParser = new PIDParser(attVal);
							pidParser.disseminationURI();
							String objectId = pidParser.getObjectId();
							//LOGGER.info("processing uuid =" +objectId);
							Document relsExt = getRelsExt(objectId);
							processingStack.push(relsExt.getDocumentElement());
						}
					} else {
						LOGGER.severe("Unsupported type of relation");
					}
					NodeList childNodes = topElem.getChildNodes();
					for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
						Node item = childNodes.item(i);
						if (item.getNodeType() == Node.ELEMENT_NODE) {
							processingStack.push((Element) item);
						}
					}
				} else if (namespaceURI.equals(FedoraNamespaces.RDF_NAMESPACE_URI)) {
					NodeList childNodes = topElem.getChildNodes();
					for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
						Node item = childNodes.item(i);
						if (item.getNodeType() == Node.ELEMENT_NODE) {
							processingStack.push((Element) item);
						}
					}
				}
			}
		} catch (DOMException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (LexerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}
	}

	@Override
	public void processRelsExt(String uuid, RelsExtHandler handler) throws IOException {
		LOGGER.info("processing uuid =" +uuid);
		processRelsExt(getRelsExt(uuid), handler);
	}

	@Override
	public List<Element> getPages(String uuid, Element rootElementOfRelsExt)
			throws IOException {
		try {
			ArrayList<Element> elms = new ArrayList<Element>();
			String xPathStr = "/RDF/Description/hasPage";
			XPathFactory xpfactory = XPathFactory.newInstance();
			XPath xpath = xpfactory.newXPath();
			XPathExpression expr = xpath.compile(xPathStr);
			NodeList nodes = (NodeList) expr.evaluate(rootElementOfRelsExt, XPathConstants.NODESET);
			for (int i = 0,lastIndex=nodes.getLength()-1; i <= lastIndex; i++) {
				Element elm = (Element) nodes.item(i);
				elms.add(elm);
			}
			return elms;
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}
	}


	@Override
	public InputStream getThumbnail(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(getThumbnailFromFedora(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream thumbInputStream = con.getInputStream();
		return thumbInputStream;
	}
	
	public InputStream getDJVU(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(getDjVuImage(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream thumbInputStream = con.getInputStream();
		return thumbInputStream;
	}

	public static String biblioMods(KConfiguration configuration, String uuid) {
		String fedoraObject = configuration.getFedoraHost() +"/get/uuid:"+uuid;
		return fedoraObject + "/BIBLIO_MODS";
	}

	public static String dc(KConfiguration configuration, String uuid) {
		String fedoraObject = configuration.getFedoraHost() +"/get/uuid:"+uuid;
		return fedoraObject + "/DC";
	}

	public static String relsExtUrl(KConfiguration configuration, String uuid) {
		String url = configuration.getFedoraHost() +"/get/uuid:"+uuid+"/RELS-EXT";
		return url;
	}
}
