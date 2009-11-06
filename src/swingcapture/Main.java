/*
 * Main.java
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

import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author santa
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                final CaptureFrame frame = new CaptureFrame();
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent evt) {
                        frame.close();
                        System.exit(0);
                    }
                });
                frame.setVisible(true);
            }
        });
    }

}
