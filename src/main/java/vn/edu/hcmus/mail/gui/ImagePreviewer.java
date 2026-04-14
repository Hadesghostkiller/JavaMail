package vn.edu.hcmus.mail.gui;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class ImagePreviewer extends JComponent implements PropertyChangeListener {
    private ImageIcon thumbnail = null;
    private File file = null;

    public ImagePreviewer(JFileChooser fc) {
        setPreferredSize(new Dimension(200, 200)); // Độ rộng của bảng xem trước
        fc.addPropertyChangeListener(this);
    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }
        ImageIcon tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon.getIconWidth() > 190) {
            // Thu nhỏ ảnh cho vừa cái khung preview
            thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(190, -1, Image.SCALE_DEFAULT));
        } else {
            thumbnail = tmpIcon;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        // Khi người dùng click chọn một file khác
        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (thumbnail == null) loadImage();
        if (thumbnail != null) {
            int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
            int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;
            if (y < 0) y = 0;
            if (x < 0) x = 0;
            thumbnail.paintIcon(this, g, x, y);
        }
    }
}