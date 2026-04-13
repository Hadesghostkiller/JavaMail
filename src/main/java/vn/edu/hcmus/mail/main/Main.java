package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.gui.MainFrame;

import javax.swing.SwingUtilities;

import vn.edu.hcmus.mail.AttachmentManager.AttachmentTask;

public class Main {
    public static void main(String[] args) {

        MainFrame.setupLook();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        // Gui + Dinh kem file
        AttachmentTask myTask = new AttachmentTask();
        myTask.AttachmentRun();
    }
}