/*
 * CapturePanel.java
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

package swingcapture;

import com.sun.image.codec.jpeg.*;
import javax.media.*;
import javax.media.format.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

/**
 *
 * @author santa
 */
public class CapturePanel extends JPanel {

    // TODO: set dynamically from registry
    public static final Dimension SIZE = new Dimension(640, 480);
    
    private String camera;
    private Dimension size;
    private DataSource ds;
    private Player player;

    protected Component visual;

    private Image img;
    private Buffer buf;
    private BufferToImage btoi;

    /**
     * Construct a {@code CapturePanel} from a capture device's name or its
     * media locator string. Once created, a player for the device can be
     * embedded onto the panel and started.
     *
     * @param camera the capture device's name or its media locator
     * @param size the preferred size of the camera
     */
    public CapturePanel(String camera, Dimension size) throws NoPlayerException {
        setLayout(new BorderLayout());
        setPreferredSize(SIZE);

        this.camera = camera;
        this.size = size;
        ds = null;
        player = null;
        visual = null;

        img = null;
        buf = null;
        btoi = null;
    }

    /**
     * Create and start the capture device's player.
     */
    public void playerStart() throws NoPlayerException {
        CaptureDeviceInfo cdi = CaptureDeviceManager.getDevice(camera);
        MediaLocator ml;
        if (cdi == null)
            ml = new MediaLocator(camera);
        else
            ml = cdi.getLocator();

        try {
            ds = Manager.createDataSource(ml);
            for (Format format : getSupportedFormats()) {
                if (format instanceof VideoFormat) {
                    Dimension supportedSize = ((VideoFormat) format).getSize();
                    if (supportedSize.equals(size)) {
                        if (requestCaptureFormat(format)) {
                            System.out.println("Resolution set to " + size);
                            setPreferredSize(size);
                        }
                    }
                }
            }
            player = Manager.createRealizedPlayer(ds);
        } catch (NoPlayerException ex) {
            System.err.println(String.format("No device found with name '%s'",
                    camera));
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        player.start();
        visual = player.getVisualComponent();

        if (visual != null) {
            add(visual, BorderLayout.CENTER);
            revalidate();
            System.out.println(String.format("Camera visual added for '%s'",
                        camera));
        } else
            System.err.println(String.format("Cannot attach camera visual for '%s'",
                    camera));
    }

    /**
     * Return all {@code Format}s supported by the underlying data source.
     * Since an existing {@code DataSource} object must have been created by
     * this panel, this method can only return meaningful values after the first
     * call to {@link CapturePanel#playerStart()}.
     *
     * @return all {@code Format}s supported by the underlying data source
     */
    public Format[] getSupportedFormats() {
        if (ds instanceof CaptureDevice) {
            FormatControl[] fcs = ((CaptureDevice) ds).getFormatControls();
            for (FormatControl fc : fcs) {
                Format[] formats = fc.getSupportedFormats();
                return formats;
            }
        }

        return null;
    }

    /**
     * Attempt to set the format of the capture device to the requested format.
     * Return {@code true} if successful, and {@code false} otherwise.
     *
     * @param request the requested format
     * @return {@code true} if successful in changing to requested format
     */
    private boolean requestCaptureFormat(Format request) {
        if (ds instanceof CaptureDevice && request != null) {
            FormatControl[] fcs = ((CaptureDevice) ds).getFormatControls();
            for (FormatControl fc : fcs) {
                Format[] formats = ((FormatControl) fc).getSupportedFormats();
                for (Format format : formats)
                    if (format.matches(request)) {
                        Format set = ((FormatControl) fc).setFormat(format);
                        return format.matches(set);
                    }
            }
        }

        return false;
    }

    /**
     * Close and deallocate the running player, if it exists.
     */
    public void playerClose() {
        if (player != null) {
            player.close();
            player.deallocate();
            
            player = null;
            ds = null;
            img = null;
            buf = null;
            btoi = null;

            remove(visual);
            revalidate();
            visual = null;
        }
    }

    /**
     * Capture the current frame from the capture device into an {@link Image}
     * object.
     *
     * @return the {@code Image} object representing the captured frame
     */
    public Image capture() {
        // grab a frame
        FrameGrabbingControl fgc = (FrameGrabbingControl)
                player.getControl("javax.media.control.FrameGrabbingControl");
        if (fgc == null)
            throw new RuntimeException("FrameGrabbingControl is not supported");
        buf = fgc.grabFrame();

        // convert to image
        btoi = new BufferToImage((VideoFormat) buf.getFormat());
        img = btoi.createImage(buf);

        return img;
    }

    /**
     * A helper function that encodes the given {@link Image} object in JPEG
     * format and saves it to the given file.
     *
     * @param img the image object to encode
     * @param file the output file
     */
    public static void saveJPG(Image img, String file) {
        BufferedImage bi = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, null, null);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            System.err.println(String.format("File not found: %s", file));
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
    }

    // for unit testing ...
    public static void main(String[] args) throws Exception {
        System.out.println("CAPTURE VIDEO FORMAT: RGB");
        Vector<CaptureDeviceInfo> devices = CaptureDeviceManager.getDeviceList(
                new VideoFormat(VideoFormat.RGB));
        
        if (devices.isEmpty()) {
            System.out.println("NO VIDEO FORMAT: RGB");
            System.exit(0);
        }
        
        for (CaptureDeviceInfo device : devices)
            System.out.println(String.format("Found device: '%s'",
                    device.getName()));

        final Frame f = new Frame("Testing CapturePanel");
        final CapturePanel cp = new CapturePanel("vfw://0", SIZE);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cp.playerClose();
                System.exit(0);
            }
        });

        f.setSize(640, 480);
        f.setLayout(new BorderLayout());
        f.add(cp, BorderLayout.CENTER);

        f.pack();
        f.setVisible(true);

        cp.playerStart();
    }

}

