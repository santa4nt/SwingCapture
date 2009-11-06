/*
 * CaptureSaver.java
 *
 * Copyright (c) 2009 santa. All rights reserved.
 *
 * This file is part of SwingCapture.
 *
 * SwingCapture is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SwingCapture is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwingCapture.  If not, see <http://www.gnu.org/licenses/>.
 */

package swingcapture.util;

import com.sun.image.codec.jpeg.*;
import flickr.Flickr;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 *
 * @author santa
 */
public class CaptureSaver {

    private static final String KEY = "b372801e1f783d3c29df83d0a2a86242";
    private static final String SECRET = "8097715d062188fb";

    /**
     * A utility function that encodes the given {@link Image} object in JPEG
     * format and saves it to the given file in an {@code ./captures/) folder.
     * Assume that the directory already exists.
     *
     * @param img the image object to encode
     * @param file the output file
     * @return the path where the file is saved
     * @throws java.io.FileNotFoundException if the file cannot be created
     */
    public static String saveJPG(Image img, String file)
            throws FileNotFoundException {

        BufferedImage bi = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, null, null);

        String directory = "captures";

        FileOutputStream out = null;
        String save = directory + File.separator + file;
        try {
            out = new FileOutputStream(save);
        } catch (FileNotFoundException ex) {
            System.err.println(String.format("File not found: %s", save));
            throw ex;
        }

        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
        param.setQuality(0.5f, false);
        encoder.setJPEGEncodeParam(param);

        try {
            encoder.encode(bi);
            out.close();
        } catch (IOException ex)  {
            System.err.println(String.format("Error encoding to file: %s", file));
            ex.printStackTrace();
            return "";
        }

        return save;
    }

    /**
     * A utility function that uploads the given file to Flickr.
     *
     * @param file the image file
     */
    public static void uploadJPG(String file) {
        final String source = file;
//        final InputStream data = new FileInputStream(file);
//        final UploadMetaData meta = new UploadMetaData();
//        meta.setTitle(title);
//        meta.setDescription(description);
//        meta.setFamilyFlag(true);
//        meta.setFriendFlag(true);
//        meta.setPublicFlag(true);
//        meta.setHidden(true);
//        Vector<String> tags = new Vector<String>();
//        tags.add("party");
//        tags.add("triangle");
//        tags.add("bruins");
//        tags.add("triangle");
//        meta.setTags(tags);

        Thread runner = new Thread(new Runnable() {
            public void run() {
                Flickr.getFlickr(KEY, SECRET).uploadPicture(source);
                System.out.println("Image " + source + " successfully uploaded to Flickr");
            }
        });
        runner.start();
    }

}
