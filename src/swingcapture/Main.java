/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swingcapture;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Widget
 */
public class Main {

    // for convenience only (TODO: query registry and present choices)
    public static final String CAMERA = "vfw:Microsoft WDM Image Capture (Win32):0";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final Frame f = new Frame("CaptureCam");
        final CapturePanel cp = new CapturePanel(CAMERA);

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
