/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swingcapture;

import com.sun.image.codec.jpeg.*;
import javax.media.*;
import javax.media.format.*;
import javax.media.control.*;
import javax.media.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

/**
 *
 * @author santa
 */
public class CapturePanel extends Panel {
    
    private Player player;
    private String camera;

    private Image img;
    private Buffer buf;
    private BufferToImage btoi;

    /**
     * Construct a {@code CapturePanel} from a capture device's name or its
     * location, as related to its URL. Once created, a player for the device
     * is embedded onto the panel and is started.
     *
     * @param camera the capture device's name or its URL
     */
    public CapturePanel(String camera) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(640, 480));

        this.camera = camera;
        playerStart();

        img = null;
        buf = null;
        btoi = null;
    }

    /**
     * Create and start the capture device's player.
     */
    public void playerStart() {
        CaptureDeviceInfo cdi = CaptureDeviceManager.getDevice(camera);
        MediaLocator ml;
        if (cdi == null)
            ml = new MediaLocator(camera);
        else
            ml = cdi.getLocator();
        if (ml == null)
            throw new RuntimeException(String.format(
                    "No device found with name '%s'", camera));

        try {
            player = Manager.createRealizedPlayer(ml);
            player.start();
            Component comp = player.getVisualComponent();

            if (comp != null) {
                add(comp, BorderLayout.CENTER);
                System.out.println(String.format("Camera visual added for '%s'",
                        camera));
            } else
                System.err.println(String.format("Cannot attach camera visual for '%s'",
                        camera));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close and deallocate the running player, if it exists.
     */
    public void playerClose() {
        if (player != null) {
            player.close();
            player.deallocate();
            player = null;
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
    public static void main(String[] args) {
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
        final CapturePanel cp = new CapturePanel("vfw://0");

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
    }

}

