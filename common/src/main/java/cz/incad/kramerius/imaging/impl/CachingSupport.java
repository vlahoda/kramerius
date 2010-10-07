package cz.incad.kramerius.imaging.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.output.FileWriterWithEncoding;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class CachingSupport {

    
    private static final String DEEP_ZOOM_DESC_FILE = "deep_zoom";
    
    
    public void writeDeepZoomDescriptor(String uuid, Image rawImage, int tileSize) throws IOException {
        StringTemplate template = new StringTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Image TileSize=\"$tileSize$\" Overlap=\"0\" Format=\"jpg\" xmlns=\"http://schemas.microsoft.com/deepzoom/2008\"><Size Width=\"$width$\" Height=\"$height$\"/></Image>");
        template.setAttribute("tileSize", tileSize);
        template.setAttribute("width", rawImage.getWidth(null));
        template.setAttribute("height", rawImage.getHeight(null));
        File deepZoom = getDeepZoomDescriptor(uuid);
        if (!deepZoom.exists()) {
            deepZoom.createNewFile();
        }
        FileWriterWithEncoding writer = new FileWriterWithEncoding(deepZoom, "UTF-8");
        try {
            writer.write(template.toString());
        } finally {
            writer.close();
        }
        
    }

    public boolean isDeepZoomFullImagePresent(String uuid) throws IOException {
        File rawImageFile = getRawImageFile(uuid);
        return rawImageFile.exists() && rawImageFile.canRead();
    }

    public File getRawImageFile(String uuid) throws IOException {
        File folder = getOneImageFolder(uuid);
        File rawImageFile = new File(folder, uuid);
        return rawImageFile;
    }
    
    public void writeDeepZoomFullImage(String uuid, Image rawImage, float quality) throws IOException {
        File rawImageFile = getRawImageFile(uuid);
//        FileOutputStream fos = new FileOutputStream(rawImageFile);
        FileImageOutputStream fosI = new FileImageOutputStream(rawImageFile);
        try {
            KrameriusImageSupport.writeImageToStream(rawImage, "JPG", fosI,quality);
        } finally {
            if (fosI != null) {
                fosI.close();
            }
        }
    }

    private synchronized File getImagingStorageFolder() throws IOException {
        String imagingStorage = KConfiguration.getInstance().getDeepZoomCacheDir();
        File folder = new File(imagingStorage);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                throw new IOException("cannot create folder '"+folder.getAbsolutePath()+"'");
            }
        }
        return folder;
    }
    
    private File getOneImageFolder(String uuid) throws IOException {
        File folder = getImagingStorageFolder();
        File oneImageFolder = new File(folder, uuid);
        if (!oneImageFolder.exists()) {
            boolean created = oneImageFolder.mkdirs();
            if (!created) {
                throw new IOException("cannot create folder '"+folder.getAbsolutePath()+"'");
            }
        }
        return oneImageFolder;
    }

    public void writeDeepZoomTile(String uuid, int level, int row, int col, Image tileImage, float quality) throws IOException {
        File folder = getTileImageFolder(uuid, level);
        File tileImageFile = new File(folder, getTileName(row, col));
        //FileOutputStream fos = new FileOutputStream(tileImageFile);
        FileImageOutputStream fosI = new FileImageOutputStream(tileImageFile);
        try {
            KrameriusImageSupport.writeImageToStream(tileImage, "JPG", fosI,quality);
        } finally {
            if (fosI != null) {
                fosI.close();
            }
        }
    }

    private String getTileName(int row, int col) {
        return row+"_"+col;
    }
    
    
    public boolean isDeepZoomTilePresent(String uuid, int level, int row, int col) throws IOException {
        File tileImageFolder = getTileImageFolder(uuid, level);
        File tileImageFile = new File(tileImageFolder, getTileName(row, col));
        if ((tileImageFile.exists()) && (tileImageFile.canRead())) {
            return true;
        } else return false;
    }

    private File getTileImageFolder(String uuid, int level) throws IOException {
        File oneImgFolder = getOneImageFolder(uuid);
        File levelFolder = new File(oneImgFolder, ""+level);
        if (!levelFolder.exists()) {
            boolean created = levelFolder.mkdirs();
            if (!created) {
                throw new IOException("cannot create folder '"+levelFolder.getAbsolutePath()+"'");
            }
        }
        return levelFolder;
    }

    public boolean isDeepZoomDescriptionPresent(String uuid) throws IOException {
        File deepZoom = getDeepZoomDescriptor(uuid);
        return deepZoom.exists() && deepZoom.canRead();
    }

    private File getDeepZoomDescriptor(String uuid) throws IOException {
        File folder = getOneImageFolder(uuid);
        File deepZoom = new File(folder, DEEP_ZOOM_DESC_FILE);
        return deepZoom;
    }

    public InputStream openDeepZoomDescriptor(String uuid) throws FileNotFoundException, IOException {
        return new FileInputStream(getDeepZoomDescriptor(uuid));
    }
    
    
    public void writeLevels(String uuid, int levels) throws IOException {
        File oneImageFolder = getOneImageFolder(uuid);
        File levelFile = new File(oneImageFolder, "levels_"+levels);
        boolean created = levelFile.createNewFile();
        if (!created) {
            throw new IOException("cannot create file '"+levelFile.getAbsolutePath()+"'");
        }
    }

    public File getDeepZoomLevelsFile(String uuid) throws IOException {
        File oneImageFolder = getOneImageFolder(uuid);
        File[] files = oneImageFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith("levels_")) {
                    return true;
                } else return false;
            }
        });
        if ((files != null) && (files.length > 0)) {
            return files[0];
        } else return null;
    }

    
    public InputStream openDeepZoomTile(String uuid, int level, int row,
            int col) throws IOException {
        File folder = getTileImageFolder(uuid, level);
        File tileImageFile = new File(folder, getTileName(row, col));
        return new FileInputStream(tileImageFile);
    }

    
}
