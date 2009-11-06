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
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 *
 * @author santa
 */
public class CaptureSaver {

    /**
     * A utility function that encodes the given {@link Image} object in JPEG
     * format and saves it to the given file in an {@code ./captures/) folder.
     * Assume that the directory already exists.
     *
     * @param img the image object to encode
     * @param file the output file
     * @throws java.io.FileNotFoundException if the file cannot be created
     */
    public static void saveJPG(Image img, String file)
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
        }

        System.out.println("Image capture saved to " + save);
    }

}
