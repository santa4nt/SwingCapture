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

import javax.media.*;
import javax.media.format.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 *
 * @author santa
 */
public class CapturePanel extends JPanel {

    private final CaptureDeviceInfo camera;
    private final Format format;
    private DataSource ds;
    private Player player;

    protected Component visual;

    private Image img;
    private Buffer buf;
    private BufferToImage btoi;

    /**
     * Construct a {@code CapturePanel} from the given capture device and its
     * supported format. Once created, a player for the device can be embedded
     * onto the panel and started.
     *
     * @param camera the video capture device
     * @param format the supported format for the capture device
     * @throws NoPlayerException
     */
    public CapturePanel(CaptureDeviceInfo camera, Format format) {
        assert (format instanceof VideoFormat);
        setLayout(new BorderLayout());
        setPreferredSize(((VideoFormat) format).getSize());

        this.camera = camera;
        this.format = format;
        ds = null;
        player = null;
        visual = null;

        img = null;
        buf = null;
        btoi = null;
    }

    /**
     * Based on the {@code CaptureDeviceInfo} and {@code Format} objects
     * specified through the constructor, create and realize a
     * {@code DataSource} and its related {@code Player}.
     */
    private void createDataSource() throws NoPlayerException {
        assert (camera != null);
        MediaLocator ml = camera.getLocator();
        try {
            ds = Manager.createDataSource(ml);
            assert (format instanceof VideoFormat);
            VideoFormat vf = (VideoFormat) format;
            if (requestCaptureFormat(vf)) {
                System.out.println("Resolution set to " + vf.getSize());
                System.out.println("Format set to " + vf.toString());
                setPreferredSize(vf.getSize());
            }

            player = Manager.createRealizedPlayer(ds);
        } catch (NoPlayerException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Create and start the capture device's player.
     */
    public void playerStart() throws NoPlayerException {
        // close the player first, if it hadn't been already (moot at first)
        playerClose();
        
        // create the data source and player objects
        createDataSource();

        player.start();
        visual = player.getVisualComponent();

        if (visual != null) {
            add(visual, BorderLayout.CENTER);
            revalidate();
            System.out.println(String.format("Camera visual added for '%s'",
                        camera.getName()));
        } else
            System.err.println(String.format("Cannot attach camera visual for '%s'",
                    camera.getName()));

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
        Vector<Format> allFormats = new Vector<Format>();
        if (ds instanceof CaptureDevice) {
            FormatControl[] fcs = ((CaptureDevice) ds).getFormatControls();
            for (FormatControl fc : fcs) {
                Format[] formats = fc.getSupportedFormats();
                for (int i = 0; i < formats.length; i++)
                    allFormats.add(formats[i]);
            }

            Format[] supportedFormats = new Format[allFormats.size()];
            return allFormats.toArray(supportedFormats);
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
                for (Format fmt : formats)
                    if (fmt.matches(request)) {
                        Format set = ((FormatControl) fc).setFormat(fmt);
                        return fmt.matches(set);
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
        }

        player = null;
        ds = null;
        img = null;
        buf = null;
        btoi = null;

        if (visual != null) {
            remove(visual);
            revalidate();
        }

        visual = null;
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

}

