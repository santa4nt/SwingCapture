/*
 * VideoChooser.java
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

import java.util.Vector;
import javax.media.*;
import javax.media.format.*;

/**
 *
 * @author santa
 */
public class VideoChooser extends javax.swing.JDialog {

    private Vector<CaptureDeviceInfo> vidDevices;
    private Vector<Format> vidFormats;

    private CaptureDeviceInfo selectedVideoDevice;
    private Format selectedVideoFormat;

    /** Creates new form VideoChooser */
    public VideoChooser(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        populateDevices();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        videoDevices = new javax.swing.JComboBox();
        videoFormats = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        cancel = new javax.swing.JButton();
        ok = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Video Chooser"); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setModal(true);
        setName("videoChooser"); // NOI18N
        setResizable(false);

        videoDevices.setName("videoDevices"); // NOI18N
        videoDevices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                videoDevicesActionPerformed(evt);
            }
        });

        videoFormats.setName("videoFormats"); // NOI18N
        videoFormats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                videoFormatsActionPerformed(evt);
            }
        });

        jLabel1.setText("Video Device(s)");
        jLabel1.setFocusable(false);

        jLabel2.setText("Video Format(s)");
        jLabel2.setFocusable(false);

        cancel.setText("Cancel");
        cancel.setName("cancel"); // NOI18N
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        ok.setText("OK");
        ok.setName("ok"); // NOI18N
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(videoFormats, 0, 307, Short.MAX_VALUE)
                            .addComponent(videoDevices, 0, 307, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(videoDevices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(videoFormats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel)
                    .addComponent(ok))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Query the JMF Registry for video devices, and populate the combo box.
     */
    private void populateDevices() {
        Vector<CaptureDeviceInfo> devices =
                CaptureDeviceManager.getDeviceList(null);

        // get the list of video devices, and add to combo box
        vidDevices = new Vector<CaptureDeviceInfo>();
        for (CaptureDeviceInfo device : devices) {
            Format[] formats = device.getFormats();
            for (Format format : formats) {
                if (format instanceof VideoFormat) {
                    vidDevices.add(device);
                    videoDevices.addItem(device.getName());
                    break;
                }
            }
        }

        populateFormats();
    }

    /**
     * Given the currently selected video device from the combo box, query
     * its available formats and populate the formats combo box.
     */
    private void populateFormats() {
        videoFormats.removeAllItems();
        int selected = videoDevices.getSelectedIndex();
        if (selected != -1) {
            CaptureDeviceInfo cdi = vidDevices.elementAt(selected);
            if (cdi != null) {
                Format[] formats = cdi.getFormats();
                vidFormats = new Vector<Format>();
                for (Format format : formats) {
                    vidFormats.add(format);
                    videoFormats.addItem(format.toString());
                }
            }
        }
    }

    /**
     * Return the {@code CaptureDeviceInfo} object representing the selected
     * video device.
     *
     * @return the video device's {@code CaptureDeviceInfo} object
     */
    public CaptureDeviceInfo getVideoDevice() {
        return selectedVideoDevice;
    }

    /**
     * Return the {@code Format} object representing the selected video format.
     *
     * @return the video format's {@code Format} object
     */
    public Format getVideoFormat() {
        return selectedVideoFormat;
    }

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        int selected = videoDevices.getSelectedIndex();
        if (selected != -1)
            selectedVideoDevice = vidDevices.elementAt(selected);
        
        selected = videoFormats.getSelectedIndex();
        if (selected != -1)
            selectedVideoFormat = vidFormats.elementAt(selected);

        setVisible(false);
    }//GEN-LAST:event_okActionPerformed

    private void videoDevicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_videoDevicesActionPerformed
        populateFormats();
    }//GEN-LAST:event_videoDevicesActionPerformed

    private void videoFormatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_videoFormatsActionPerformed
        // do nothing
    }//GEN-LAST:event_videoFormatsActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                VideoChooser dialog = new VideoChooser(new javax.swing.JFrame());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);

                int rc = 0;
                try {
                    System.out.println("Selected video device: "
                            + dialog.getVideoDevice().getName());
                    System.out.println("Selected video format: "
                            + dialog.getVideoFormat().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    rc = 1;
                } finally {
                    System.exit(rc);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JButton ok;
    private javax.swing.JComboBox videoDevices;
    private javax.swing.JComboBox videoFormats;
    // End of variables declaration//GEN-END:variables

}
