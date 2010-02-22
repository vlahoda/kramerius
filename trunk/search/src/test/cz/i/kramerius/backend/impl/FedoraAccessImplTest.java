package cz.i.kramerius.backend.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.Kramerius.backend.impl.FedoraAccessImpl;
import cz.incad.utils.XMLUtils;


public class FedoraAccessImplTest {
	
	@Test
	public void getPagesTest() throws IOException, ParserConfigurationException, SAXException {
		InputStream stream = this.getClass().getResourceAsStream("rels-ext.xml");
		Document document = XMLUtils.parseDocument(stream);	
		FedoraAccessImpl fi = new FedoraAccessImpl(null);
		List<Element> pages = fi.getPages(null, document.getDocumentElement());
		assertNotNull(pages);
		assertTrue(!pages.isEmpty());
		assertTrue(pages.size() == 16);
	}
}
