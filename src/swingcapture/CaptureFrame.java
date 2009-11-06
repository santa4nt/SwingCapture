/*
 * CaptureFrame.java
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
import java.io.*;
import java.text.*;
import java.util.*;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.NoPlayerException;
import javax.swing.*;
import static swingcapture.util.CaptureSaver.saveJPG;
import static swingcapture.util.CaptureSaver.uploadJPG;

/**
 *
 * @author santa
 */
public class CaptureFrame extends JFrame {

    private JButton setup;
    private JButton start;
    private JButton stop;
    private JButton capture;
    private VideoChooser chooser;
    private CapturePanel capturePanel;
    
    public CaptureFrame() {
        super();
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Layout code">
    private void initComponents() {
        setup = new JButton();
        start = new JButton();
        stop = new JButton();
        capture = new JButton();
        chooser = new VideoChooser(this);

        setTitle("Swing Capture");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setup.setText("Set up device ...");
        setup.setName("setup");
        setup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setupActionPerformed(evt);
            }
        });
        
        start.setText("Start");
        start.setName("start");
        start.setEnabled(false);
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startActionPerformed(evt);
            }
        });
        
        stop.setText("Stop");
        stop.setName("stop");
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopActionPerformed(evt);
            }
        });
        
        capture.setText("Capture");
        capture.setName("capture");
        capture.setEnabled(false);
        capture.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                captureActionPerformed(evt);
            }
        });

        JPanel control = new JPanel();
        control.setLayout(new FlowLayout(FlowLayout.CENTER));
        control.add(setup);
        control.add(start);
        control.add(stop);
        control.add(capture);

        BorderLayout layout = new BorderLayout();
        Container contentPane = getContentPane();
        contentPane.setLayout(layout);

        // TODO: layout
        contentPane.add(control, BorderLayout.SOUTH);

        pack();
    }// </editor-fold>

    private void setupActionPerformed(ActionEvent evt) {
        assert (chooser != null);
        chooser.setVisible(true);
        if (chooser.getVideoDevice() != null) {
            start.setEnabled(true);
            stop.setEnabled(false);
            capture.setEnabled(false);
        }
    }

    private void startActionPerformed(ActionEvent evt) {
        start.setEnabled(false);
        setup.setEnabled(false);
        stop.setEnabled(true);
        capture.setEnabled(true);

        CaptureDeviceInfo cdi = chooser.getVideoDevice();
        Format format = chooser.getVideoFormat();
        assert (cdi != null && format != null);
        capturePanel = new CapturePanel(cdi, format);
        
        // TODO: move to EDT
        try {
            capturePanel.playerStart();
        } catch (NoPlayerException ex ) {
            ex.printStackTrace();   // TODO: error dialog box instead
            start.setEnabled(false);
            return;
        }
        getContentPane().add(capturePanel, BorderLayout.NORTH);
        ((JComponent) getContentPane()).revalidate();
        pack();
    }

    private void stopActionPerformed(ActionEvent evt) {
        stop.setEnabled(false);
        capture.setEnabled(false);
        setup.setEnabled(true);
        capturePanel.playerClose();
        
        // TODO: move to EDT
        getContentPane().remove(capturePanel);
        ((JComponent) getContentPane()).revalidate();
        pack();

        start.setEnabled(true);
    }

    private void captureActionPerformed(ActionEvent evt) {
        capture.setEnabled(false);

        // TODO: self-closing dialog box, with timer
        JOptionPane.showMessageDialog(this, "Image will be captured in 3 seconds ...",
                "Image Capture", JOptionPane.INFORMATION_MESSAGE);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            capture.setEnabled(true);
            return;
        }

        Image captured = capturePanel.capture();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        final String timestamp = sdf.format(cal.getTime());
        final String outfile = "CPT-" + timestamp + ".jpg";
        try {
            final String saved = saveJPG(captured, outfile);
            System.out.println("Image capture saved to " + saved);
            JOptionPane.showMessageDialog(this, "Image successfully captured.",
                "Image Capture", JOptionPane.INFORMATION_MESSAGE);

            // upload to flickr
            //uploadJPG(saved, "Mad Scientist Mayhem", "Taken at " + sdf);
            uploadJPG(saved);
        } catch (FileNotFoundException ex) {
            // TODO: error dialog box instead
            String message = "Cannot save to " + outfile;
            System.err.println(message);
            JOptionPane.showMessageDialog(this, message,
                    "Image Capture", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            capture.setEnabled(true);
        }
    }

    /**
     * Release resources and dispose the window.
     */
    public void close() {
        if (capturePanel != null) {
            capturePanel.playerClose();
            getContentPane().remove(capturePanel);
        }
        
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
