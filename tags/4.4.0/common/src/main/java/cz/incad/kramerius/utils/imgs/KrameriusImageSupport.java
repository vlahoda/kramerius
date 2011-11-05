package cz.incad.kramerius.utils.imgs;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JPanel;
import javax.xml.xpath.XPathExpressionException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.lizardtech.djvu.DjVuInfo;
import com.lizardtech.djvu.DjVuOptions;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuImage;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.fedora.Handler;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class KrameriusImageSupport {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KrameriusImageSupport.class.getName());

    static {
        // disable djvu convertor verbose logging
        DjVuOptions.out = new java.io.PrintStream(new java.io.OutputStream() {
            public void write(int b) {
            }
        });
    }

    public static BufferedImage readImage(String pid, String stream, FedoraAccess fedoraAccess, int page) throws XPathExpressionException, IOException {
        String mimetype = fedoraAccess.getMimeTypeForStream(pid, stream);
        //TODO: change logging
        LOGGER.fine("mimetype for pid '"+pid+"' is '"+mimetype+"'");
        ImageMimeType loadFromMimeType = ImageMimeType.loadFromMimeType(mimetype);
        URL url = new URL("fedora", "", 0, pid + "/" + stream, new Handler(fedoraAccess));
        return readImage(url, loadFromMimeType, page);
    }

    public static Dimension readDimension(String pid, String stream, FedoraAccess fedoraAccess, int page) throws XPathExpressionException, IOException {
        String mimetype = fedoraAccess.getMimeTypeForStream(pid, stream);
        ImageMimeType loadFromMimeType = ImageMimeType.loadFromMimeType(mimetype);
        URL url = new URL("fedora", "", 0, pid + "/" + stream, new Handler(fedoraAccess));
        return readDimension(url, loadFromMimeType);
    }

    public static boolean useCache(){
        return KConfiguration.getInstance().getConfiguration().getBoolean("convert.useCache", true);
    }

    public static BufferedImage readImage(URL url, ImageMimeType type, int page) throws IOException {
        LOGGER.fine("type is "+type);
        if (type == null) return null;
        if (type.javaNativeSupport()) {
            InputStream stream = url.openStream();
            try{
                ImageIO.setUseCache(useCache());
                return ImageIO.read(stream);
            }finally{
                org.apache.commons.io.IOUtils.closeQuietly(stream);
            }
        } else if ((type.equals(ImageMimeType.DJVU)) || (type.equals(ImageMimeType.VNDDJVU)) || (type.equals(ImageMimeType.XDJVU))) {
            com.lizardtech.djvu.Document doc = new com.lizardtech.djvu.Document(url);
            doc.setAsync(false);
            DjVuPage[] p = new DjVuPage[1];
            // read page from the document - index 0, priority 1, favorFast true
            int size = doc.size();
            if ((page != 0) && (page >= size)) {
                page = 0;
            }
            p[0] = doc.getPage(page, 1, true);
            p[0].setAsync(false);

            DjVuImage djvuImage = new DjVuImage(p, true);
            Rectangle pageBounds = djvuImage.getPageBounds(0);

            Image[] images = djvuImage.getImage(new JPanel(), new Rectangle(pageBounds.width, pageBounds.height));
            if (images.length == 1) {
                Image img = images[0];
                if (img instanceof BufferedImage) {
                    return (BufferedImage) img;
                } else {
                    return toBufferedImage(img);
                }
            } else
                return null;
        } else if (type.equals(ImageMimeType.PDF)) {
            PDDocument document = null;
            InputStream stream = url.openStream();
            try {

                document = PDDocument.load(stream);
                int resolution = 96;
                List pages = document.getDocumentCatalog().getAllPages();
                PDPage pdPage = (PDPage) pages.get(page);
                BufferedImage image = pdPage.convertToImage(BufferedImage.TYPE_INT_RGB, resolution);
                return image;
            } finally {
                if (document != null) {
                    document.close();
                }
                org.apache.commons.io.IOUtils.closeQuietly(stream);
            }
        } else
            throw new IllegalArgumentException("unsupported mimetype '" + type.getValue() + "'");
    }

    public static void writeImageToStream(BufferedImage image, String javaFormat, OutputStream os) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, javaFormat, bos);
        IOUtils.copyStreams(new ByteArrayInputStream(bos.toByteArray()), os);
    }

    public static Dimension readDimension(URL url, ImageMimeType type) throws IOException {
        if (type.javaNativeSupport()) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(type.getDefaultFileExtension());
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                ImageInputStream istream = ImageIO.createImageInputStream(url.openStream());
                reader.setInput(istream);
                int height = reader.getHeight(0);
                int width = reader.getWidth(0);
                return new Dimension(width, height);
            } else
                return null;
        } else {
            com.lizardtech.djvu.Document doc = new com.lizardtech.djvu.Document(url);
            doc.setAsync(false);
            DjVuPage page = doc.getPage(0, 1, true);
            DjVuInfo info = page.getInfoWait();
            DjVuImage djvuImage = new DjVuImage(new DjVuPage[]{page}, true);
            Rectangle pageBounds = djvuImage.getPageBounds(0);
            System.out.println(pageBounds);
            System.out.println(new Dimension(info.width, info.height));

            return new Dimension(info.width, info.height);
        }
    }

    public static void writeImageToStream(BufferedImage scaledImage, String javaFormat, FileImageOutputStream os, float quality) throws IOException {

        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(javaFormat);
        if (iter.hasNext()) {
            ImageWriter writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality); // an integer between 0 and 1
            writer.setOutput(os);
            IIOImage image = new IIOImage(scaledImage, null, null);
            writer.write(null, image, iwp);
            writer.dispose();

        } else
            throw new IOException("No writer for format '" + javaFormat + "'");

    }

    public static BufferedImage scale(BufferedImage img, int targetWidth, int targetHeight) {
        KConfiguration config = KConfiguration.getInstance();
        ScalingMethod method = ScalingMethod.valueOf(config.getProperty("scalingMethod", "BICUBIC_STEPPED"));
        boolean higherQuality = true;
        return scale(img, targetWidth, targetHeight, method, higherQuality);
    }

    public static BufferedImage scale(BufferedImage img, int targetWidth, int targetHeight, ScalingMethod method, boolean higherQuality) {
        // System.out.println("SCALE:"+method+" width:"+targetWidth+" height:"+targetHeight);
        switch (method) {
        case REPLICATE:
            Image rawReplicate = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_REPLICATE);
            if (rawReplicate instanceof BufferedImage) {
                return (BufferedImage) rawReplicate;
            } else {
                return toBufferedImage(rawReplicate);
            }
        case AREA_AVERAGING:
            Image rawAveraging = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING);
            if (rawAveraging instanceof BufferedImage) {
                return (BufferedImage) rawAveraging;
            } else {
                return toBufferedImage(rawAveraging);
            }
        case BILINEAR:
            return getScaledInstanceJava2D(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, higherQuality);
        case BICUBIC:
            return getScaledInstanceJava2D(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, higherQuality);
        case NEAREST_NEIGHBOR:
            return getScaledInstanceJava2D(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, higherQuality);
        case BILINEAR_STEPPED:
            return getScaledInstanceJava2D(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, higherQuality);
        case BICUBIC_STEPPED:
            return getScaledInstanceJava2D(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, higherQuality);
        case NEAREST_NEIGHBOR_STEPPED:
            return getScaledInstanceJava2D(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, higherQuality);
        }
        return null;
    }

    /**
     * Convenience method that returns a scaled instance of the provided
     * {@code BufferedImage}.
     *
     * @param img
     *            the original image to be scaled
     * @param targetWidth
     *            the desired width of the scaled instance, in pixels
     * @param targetHeight
     *            the desired height of the scaled instance, in pixels
     * @param hint
     *            one of the rendering hints that corresponds to
     *            {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *            {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *            {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *            {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality
     *            if true, this method will use a multi-step scaling technique
     *            that provides higher quality than the usual one-step technique
     *            (only useful in downscaling cases, where {@code targetWidth}
     *            or {@code targetHeight} is smaller than the original
     *            dimensions, and generally only when the {@code BILINEAR} hint
     *            is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    private static BufferedImage getScaledInstanceJava2D(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {

        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w > targetWidth || h > targetHeight);

        return ret;
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    public static BufferedImage getScaledInstanceJava2D(BufferedImage image, int targetWidth, int targetHeight, Object hint, GraphicsConfiguration gc) {

        // if (gc == null)
        // gc = getDefaultConfiguration();
        int w = image.getWidth();
        int h = image.getHeight();

        int transparency = image.getColorModel().getTransparency();
        // BufferedImage result = gc.createCompatibleImage(w, h, transparency);
        BufferedImage result = new BufferedImage(w, h, transparency);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        double scalex = (double) targetWidth / image.getWidth();
        double scaley = (double) targetHeight / image.getHeight();
        AffineTransform xform = AffineTransform.getScaleInstance(scalex, scaley);
        g2.drawRenderedImage(image, xform);

        g2.dispose();
        return result;
    }

    public static BufferedImage toBufferedImage(Image img) {
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }

    public static enum ScalingMethod {
        REPLICATE, AREA_AVERAGING, BILINEAR, BICUBIC, NEAREST_NEIGHBOR, BILINEAR_STEPPED, BICUBIC_STEPPED, NEAREST_NEIGHBOR_STEPPED
    }

}
