package com.qbizm.kramerius.imptool.poc;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.Monograph;
import com.qbizm.kramerius.imp.jaxb.periodical.Periodical;
import com.qbizm.kramerius.imptool.poc.convertor.MonographConvertor;
import com.qbizm.kramerius.imptool.poc.convertor.PeriodicalConvertor;
import com.qbizm.kramerius.imptool.poc.utils.ConfigurationUtils;
import com.qbizm.kramerius.imptool.poc.valueobj.ConvertorConfig;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Nastroj pro konverzi XML z Krameria do formatu Fedora Object XML
 * 
 * @author xholcik
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class);

    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;

    public static void main(String[] args) {

        if (args.length > 4) {
            System.out.println("KrameriusXML to FOXML conversion tool.\n");
            System.out.println("Usage: conversion-tool useDB defaultVisibility <input-file> [<output-folder>]");
            System.exit(1);
        }
        
        boolean useDB = Boolean.parseBoolean(args[0]);
        boolean defaultVisibility = Boolean.parseBoolean(args[1]);

        String importRoot = null;
        if (args.length == 2){
            importRoot = ConfigurationUtils.getInstance().getProperty("migration.directory");
        } else{
            importRoot = args[2];
        }
        String exportRoot = null;
        if (args.length == 4) {
            exportRoot = args[3];
        } else {
            exportRoot = importRoot + "-out";
        }

        convert(importRoot, exportRoot, useDB, defaultVisibility);

    }

    
    
    public static String convert(String importRoot, String exportRoot, boolean useDB, boolean defaultVisibility) {
        System.setProperty("java.awt.headless", "true");
        StringBuffer convertedUUID = new StringBuffer();
        if (useDB){
            initDB();
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Monograph.class, DigitalObject.class, Periodical.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");

            unmarshaller = jaxbContext.createUnmarshaller();

        } catch (Exception e) {
            log.error("Cannot init JAXB", e);
            throw new RuntimeException(e);
        }

        File importFile = new File(importRoot);
        if (!importFile.exists()) {
            System.err.println("Import root folder doesn't exist: " + importFile.getAbsolutePath());
            System.exit(1);
        }

        visitAllDirsAndFiles(importFile, importRoot, exportRoot,  useDB, defaultVisibility,  convertedUUID);
        if (conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                
            }
        }
        return convertedUUID.toString();
    }
    
    static Connection conn = null;
    
    // "jdbc:postgresql://localhost:5432/kramerius", "kramerius", "f8TasR"
    private static void initDB() {
        try {
            Class.forName(ConfigurationUtils.getInstance().getProperty("k3.db.driver"));
            String url = ConfigurationUtils.getInstance().getProperty("k3.db.url");
            String user = ConfigurationUtils.getInstance().getProperty("k3.db.user");
            String pwd = ConfigurationUtils.getInstance().getProperty("k3.db.password");
            conn = DriverManager.getConnection(url, user, pwd);
            conn.setAutoCommit(true);
            log.info("Database initialized.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    
    private static void clearExportFolder(File replicationDirectory) {
        File[] files = replicationDirectory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++)
                files[i].delete();
        }
    }

    private static void visitAllDirsAndFiles(File importFile, String importRoot, String exportRoot, boolean useDB, boolean defaultVisibility, StringBuffer convertedUUID) {

        if (importFile.isDirectory()) {
            String subFolderName = importFile.getAbsolutePath().substring(importRoot.length());

            String exportFolder = exportRoot + subFolderName;

            File exportFolderFile = new File(exportFolder);
            if (!exportFolderFile.exists() || !exportFolderFile.isDirectory()) {
                if (!exportFolderFile.mkdir()) {
                    System.err.println("Output folder doesn't exist and can't be created: " + exportFolderFile.getAbsolutePath());
                    System.exit(1);
                }
            }
            clearExportFolder(exportFolderFile);
            File[] children = importFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(children[i], importRoot, exportRoot,  useDB, defaultVisibility, convertedUUID);
            }
        } else {
            if (importFile.getName().endsWith(".xml")) {

                String importFolder = importFile.getParent();
                if (importFolder == null) {
                    importFolder = ".";
                }
                String subFolderName = importFolder.substring(importRoot.length());

                String exportFolder = exportRoot + subFolderName;

                ConvertorConfig config = new ConvertorConfig();
                config.setMarshaller(marshaller);
                config.setExportFolder(exportFolder);
                config.setImportFolder(importFolder);
                if (useDB){
                    config.setDbConnection(conn);
                }
                config.setDefaultVisibility(defaultVisibility);
                int l=5; 
                try{
                    l=Integer.parseInt(ConfigurationUtils.getInstance().getProperty("contractNo.length"));
                }catch(NumberFormatException ex){
                    log.error("Cannot parse property contractNo.length", ex);
                }
                config.setContractLength(l);
                try {
                    convertOneDirectory(unmarshaller, importFile, config, convertedUUID);
                } catch (InterruptedException e) {
                    log.error("Cannot convert "+importFile, e);
                } catch (JAXBException e) {
                    log.error("Cannot convert "+importFile, e);
                }

            }
        }
    }

    private static void convertOneDirectory(Unmarshaller unmarshaller, File importFile, ConvertorConfig config, StringBuffer convertedUUID) throws InterruptedException, JAXBException {
        long timeStart = System.currentTimeMillis();

        long before = getFreeMem();
        Object source = unmarshaller.unmarshal(importFile);
        long after = getFreeMem();
        if (log.isInfoEnabled()) {
            log.info("Memory eaten: " + ((after - before) / 1024) + "KB");
        }

        int objectCounter = 0;
        try {
            if (source instanceof Monograph) {
                MonographConvertor mc = new MonographConvertor(config);
                Monograph monograph = (Monograph) source;
                convertedUUID.append( mc.convert(monograph)).append("\n");
                objectCounter = mc.getObjectCounter();
            } else if (source instanceof Periodical) {
                PeriodicalConvertor pc = new PeriodicalConvertor(config);
                Periodical periodical = (Periodical) source;
                convertedUUID.append( pc.convert(periodical)).append("\n");
                objectCounter = pc.getObjectCounter();
            } else {
                throw new UnsupportedOperationException("Unsupported object class: " + source.getClass());
            }
        } catch (ServiceException e) {
            log.error(importFile.getName() + ": conversion failed", e);
        }
        long timeFinish = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info("Elapsed time: " + ((timeFinish - timeStart) / 1000.0) + " seconds. "+objectCounter + " digital objects (files) written.");
        }
    }

 
    /**
   * 
   */
    static class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if ("info:fedora/fedora-system:def/foxml#".equals(namespaceUri)) {
                return "foxml";
            }
            if ("http://www.loc.gov/mods/v3".equals(namespaceUri)) {
                return "mods";
            }
            return suggestion;
        }

    }

    private static long getFreeMem() throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        Thread.sleep(100);
        return runtime.totalMemory() - runtime.freeMemory();
    }

}
