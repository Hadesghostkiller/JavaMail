package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.gui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Thiết kế giao diện hiện đại
        MainFrame.setupLook();

        // Chạy GUI trên luồng an toàn (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}