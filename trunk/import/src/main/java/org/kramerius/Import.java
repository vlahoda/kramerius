package org.kramerius;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.XmlContentType;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.service.impl.SortingServiceImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;
import org.fedora.api.ObjectFactory;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Import {

    static FedoraAPIMService service;
    static FedoraAPIM port;
    static ObjectFactory of;
    static int counter = 0;
    private static final Logger log = Logger.getLogger(Import.class.getName());
    private static Unmarshaller unmarshaller = null;
    private static List<String> rootModels = null;
    private static SortingService sortingService;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);

            unmarshaller = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }

        rootModels = Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.topLevelModels"));
        if (rootModels == null) {
            rootModels = new ArrayList<String>();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String importDirectory = System.getProperties().containsKey("import.directory") ? System.getProperty("import.directory") : KConfiguration.getInstance().getProperty("import.directory");
        Import.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), importDirectory);
    }

    public static void ingest(final String url, final String user, final String pwd, String importRoot) {
        log.finest("INGEST - url:" + url + " user:" + user + " pwd:" + pwd + " importRoot:" + importRoot);
        Injector injector = Guice.createInjector(new ImportModule());
        sortingService = injector.getInstance(SortingService.class);

        // system property 
        String skipIngest = System.getProperties().containsKey("ingest.skip") ? System.getProperty("ingest.skip") : KConfiguration.getInstance().getConfiguration().getString("ingest.skip", "false");
        if (new Boolean(skipIngest)) {
            log.info("INGEST CONFIGURED TO BE SKIPPED, RETURNING");
            return;
        }
        long start = System.currentTimeMillis();

        File importFile = new File(importRoot);
        if (!importFile.exists()) {
            log.severe("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
            throw new RuntimeException("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
        }


        initialize(user, pwd);

        Set<TitlePidTuple> roots = new HashSet<TitlePidTuple>();
        Set<String> sortRelations = new HashSet<String>();
        if (importFile.isDirectory()) {
            visitAllDirsAndFiles(importFile, roots, sortRelations);
        } else {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(importFile));
            } catch (FileNotFoundException e) {
                log.severe("Import file list " + importFile + " not found: " + e);
                throw new RuntimeException(e);
            }
            try {
                for (String line; (line = reader.readLine()) != null;) {
                    if ("".equals(line)) {
                        continue;
                    }
                    File importItem = new File(line);
                    if (!importItem.exists()) {
                        log.severe("Import folder doesn't exist: " + importItem.getAbsolutePath());
                        continue;
                    }
                    if (!importItem.isDirectory()) {
                        log.severe("Import item is not a folder: " + importItem.getAbsolutePath());
                        continue;
                    }
                    log.info("Importing " + importItem.getAbsolutePath());
                    visitAllDirsAndFiles(importItem, roots, sortRelations);
                }
                reader.close();
            } catch (IOException e) {
                log.severe("Exception reading import list file: " + e);
                throw new RuntimeException(e);
            }
        }
        log.info("FINISHED INGESTION IN " + ((System.currentTimeMillis() - start) / 1000.0) + "s, processed " + counter + " files");

        String startSortProperty = System.getProperties().containsKey("ingest.sortRelations") ? System.getProperty("ingest.sortRelations") : KConfiguration.getInstance().getConfiguration().getString("ingest.sortRelations", "true");
        if (new Boolean(startSortProperty)) {


            if (sortRelations.isEmpty()) {
                log.info("NO MERGED OBJECTS FOR RELATIONS SORTING FOUND.");
            } else {
                for (String sortPid : sortRelations) {
                    sortingService.sortRelations(sortPid, false);
                }
                log.info("ALL MERGED OBJECTS RELATIONS SORTED.");
            }
        } else {
            log.info("RELATIONS SORTING DISABLED.");
        }

        String startIndexerProperty = System.getProperties().containsKey("ingest.startIndexer") ? System.getProperty("ingest.startIndexer") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer", "true");
        if (new Boolean(startIndexerProperty)) {
            if (roots.isEmpty()) {
                log.info("NO ROOT OBJECTS FOR INDEXING FOUND.");
            } else {
                StringBuilder pids = new StringBuilder();
                String pidSeparator = KConfiguration.getInstance().getConfiguration().getString("indexer.pidSeparator", "$");
                for (TitlePidTuple tpt : roots) {
                    if (pids.length()>0){
                        pids.append(pidSeparator);
                    }
                    pids.append(tpt.pid);
                }
                IndexerProcessStarter.spawnIndexer(true, importFile.getName(), pids.toString());
                log.info("ALL ROOT OBJECTS SCHEDULED FOR INDEXING.");
            }
        } else {
            log.info("AUTO INDEXING DISABLED.");
        }
    }

    public static void initialize(final String user, final String pwd) {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pwd.toCharArray());
            }
        });

        FedoraAccess fedoraAccess = null;
        try {
            fedoraAccess = new FedoraAccessImpl(null, null);
            log.info("Instantiated FedoraAccess");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot instantiate FedoraAccess", e);
            throw new RuntimeException(e);
        }
        port = fedoraAccess.getAPIM();


        of = new ObjectFactory();
    }

    private static void visitAllDirsAndFiles(File importFile, Set<TitlePidTuple> roots, Set<String> sortRelations) {
        if (importFile == null) {
            return;
        }
        if (importFile.isDirectory()) {

            File[] children = importFile.listFiles();
            if (children.length > 1 && children[0].isDirectory()) {//Issue 36
                Arrays.sort(children);
            }
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(children[i], roots, sortRelations);
            }
        } else {
            DigitalObject dobj = null;
            try {
                if (!importFile.getName().toLowerCase().endsWith(".xml")) {
                    return;
                }
                Object obj = unmarshaller.unmarshal(importFile);
                dobj = (DigitalObject) obj;
            } catch (Exception e) {
                log.info("Skipping file " + importFile.getName() + " - not an FOXML object.");
                return;
            }
            ingest(importFile, dobj.getPID(), sortRelations, roots);
            checkRoot(dobj, roots);
        }
    }

    public static void ingest(InputStream is, String pid, Set<String> sortRelations, Set<TitlePidTuple> roots) throws IOException {
        
        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copyStreams(is, bos);
        byte[] bytes = bos.toByteArray();
        try {
            port.ingest(bytes, "info:fedora/fedora-system:FOXML-1.1", "Initial ingest");
        } catch (SOAPFaultException sfex) {

            //if (sfex.getMessage().contains("ObjectExistsException")) {
            if (objectExists(pid)) {
                log.info("Merging with existing object " + pid);
                if (merge(bytes)){
                    if (sortRelations != null) {
                        sortRelations.add(pid);
                        log.info("Added merged object for sorting relations:"+pid);
                    }
                    if(roots!= null ){
                        TitlePidTuple npt = new TitlePidTuple("", pid);
                        roots.add(npt);
                        log.info("Added merged object for indexing:"+pid);
                    }
                }
            } else {

                log.severe("Ingest SOAP fault:" + sfex);
                throw new RuntimeException(sfex);
            }

        }

        counter++;
        log.info("Ingested:" + pid + " in " + (System.currentTimeMillis() - start) + "ms, count:" + counter);
    }

    public static void ingest(File file, String pid, Set<String> sortRelations, Set<TitlePidTuple> roots) {
        if (pid == null) {
            try {
                Object obj = unmarshaller.unmarshal(file);
                pid = ((DigitalObject) obj).getPID();
            } catch (Exception e) {
                log.info("Skipping file " + file.getName() + " - not an FOXML object.");
                return;
            }
        }

        try {
            FileInputStream is = new FileInputStream(file);
            ingest(is, pid, sortRelations, roots);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Ingestion error ", ex);
            throw new RuntimeException(ex);
        }
    }

    private static boolean merge(byte[] bytes) {
        List<RDFTuple> ingested = readRDF(bytes);
        if (ingested.isEmpty()) {
            return false;
        }
        String pid = ingested.get(0).subject.substring("info:fedora/".length());
        List<RelationshipTuple> existingWS = port.getRelationships(pid, null);
        List<RDFTuple> existing = new ArrayList<RDFTuple>(existingWS.size());
        for (RelationshipTuple t : existingWS) {
            existing.add(new RDFTuple(t.getSubject(), t.getPredicate(), t.getObject(), t.isIsLiteral()));
        }
        ingested.removeAll(existing);
        boolean touched = false;
        for (RDFTuple t : ingested) {
            if (t.object != null) {
                try {
                    port.addRelationship(t.subject.substring("info:fedora/".length()), t.predicate, t.object, t.literal, null);
                    touched = true;
                } catch (Exception ex) {
                    log.severe("WARNING- could not add relationship:" + t + "(" + ex + ")");
                }
            }
        }
        return touched;
    }

    private static List<RDFTuple> readRDF(byte[] bytes) {
        XMLInputFactory f = XMLInputFactory.newInstance();
        List<RDFTuple> retval = new ArrayList<RDFTuple>();
        String subject = null;
        boolean inRdf = false;
        try {
            XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(bytes));
            while (r.hasNext()) {
                r.next();
                if (r.isStartElement()) {
                    if ("rdf".equals(r.getName().getPrefix()) && "Description".equals(r.getName().getLocalPart())) {
                        subject = r.getAttributeValue(r.getNamespaceURI("rdf"), "about");
                        inRdf = true;
                        continue;
                    }
                    if (inRdf) {
                        String predicate = r.getName().getNamespaceURI() + r.getName().getLocalPart();
                        String object = r.getAttributeValue(r.getNamespaceURI("rdf"), "resource");
                        boolean literal = false;
                        if (object == null){
                            object = r.getElementText();
                            if (object != null){
                                literal = true;
                            }
                        }
                        retval.add(new RDFTuple(subject, predicate, object, literal));
                    }
                }
                if (r.isEndElement()) {
                    if ("rdf".equals(r.getName().getPrefix()) && "Description".equals(r.getName().getLocalPart())) {
                        inRdf = false;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        return retval;
    }

   /* public static void main TestReadRDF (String[] args){
        try{
            File file = new File("/Work/Kramerius/data/prvnidavka-converted/40114/0eaa6730-9068-11dd-97de-000d606f5dc6.xml");
            FileInputStream is = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(is, bos);
            byte[] bytes = bos.toByteArray();
            List<RDFTuple> rdf = readRDF(bytes);
            System.out.print(rdf);
        }catch(Throwable th){
            System.out.print(th);
        }
    }
*/
    /**
     * Parse FOXML file and if it has model in fedora.topLevelModels, add its
     * PID to roots list. Objects in the roots list then will be submitted to
     * indexer
     */
    private static void checkRoot(DigitalObject dobj, Set<TitlePidTuple> roots) {
        try {

            boolean isRootObject = false;
            String title = "";
            for (DatastreamType ds : dobj.getDatastream()) {
                if ("DC".equals(ds.getID())) {//obtain title from DC stream
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType v = versions.get(versions.size() - 1);
                        XmlContentType dcxml = v.getXmlContent();
                        List<Element> elements = dcxml.getAny();
                        for (Element el : elements) {
                            NodeList titles = el.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
                            if (titles.getLength() > 0) {
                                title = titles.item(0).getTextContent();
                            }
                        }
                    }
                }
                if ("RELS-EXT".equals(ds.getID())) { //check for root model in RELS-EXT
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType v = versions.get(versions.size() - 1);
                        XmlContentType dcxml = v.getXmlContent();
                        List<Element> elements = dcxml.getAny();
                        for (Element el : elements) {
                            NodeList types = el.getElementsByTagNameNS("info:fedora/fedora-system:def/model#", "hasModel");
                            for (int i = 0; i < types.getLength(); i++) {
                                String type = types.item(i).getAttributes().getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getNodeValue();
                                if (type.startsWith("info:fedora/model:")) {
                                    String model = type.substring(18);//get the string after info:fedora/model:
                                    isRootObject = rootModels.contains(model);
                                }
                            }
                        }
                    }
                }

            }
            if (isRootObject) {
                TitlePidTuple npt = new TitlePidTuple(title, dobj.getPID());
                if(roots!= null){
                    roots.add(npt);
                    log.info("Found object for indexing - " + npt);
                }
            }

        } catch (Exception ex) {
            log.log(Level.WARNING, "Error in Ingest.checkRoot for file " + dobj.getPID() + ", file cannot be checked for auto-indexing : " + ex);
        }
    }

    /**
     * Checks if fedora contains object with given PID
     *
     * @param pid requested PID
     * @return true if given object exists
     */
    public static boolean objectExists(String pid) {
        try {
            String fedoraObjectURL = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid;
            URLConnection urlcon = RESTHelper.openConnection(fedoraObjectURL, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            urlcon.connect();
            Object target = urlcon.getContent();
            if (target != null) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}

class RDFTuple {

    String subject;
    String predicate;
    String object;
    boolean literal;

    public RDFTuple(String subject, String predicate, String object, boolean literal) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.literal = literal;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDFTuple rdfTuple = (RDFTuple) o;

        if (literal != rdfTuple.literal) return false;
        if (object != null ? !object.equals(rdfTuple.object) : rdfTuple.object != null) return false;
        if (predicate != null ? !predicate.equals(rdfTuple.predicate) : rdfTuple.predicate != null) return false;
        if (subject != null ? !subject.equals(rdfTuple.subject) : rdfTuple.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject != null ? subject.hashCode() : 0;
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (literal ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RDFTuple{" +
                "subject='" + subject + '\'' +
                ", predicate='" + predicate + '\'' +
                ", object='" + object + '\'' +
                ", literal=" + literal +
                '}';
    }
}

class TitlePidTuple {

    public String title;
    public String pid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TitlePidTuple that = (TitlePidTuple) o;

        if (pid != null ? !pid.equals(that.pid) : that.pid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pid != null ? pid.hashCode() : 0;
    }

    public TitlePidTuple(String name, String pid) {
        this.title = name;
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "Title:" + title + " PID:" + pid;
    }
}

class ImportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
        bind(StatisticsAccessLog.class).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
        bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);
    }
}
