package vn.edu.hcmus.mail.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTextField txtTo, txtSubject;
    private JTextArea txtBody, txtInboxLog;
    private SmtpService smtpService = new SmtpService();
    private Pop3Service pop3Service  = new Pop3Service();

    // Màu sắc chủ đạo "bóng bẩy"
    private final Color PRIMARY_COLOR = new Color(33, 150, 243); // Màu xanh dương modern
    private final Color SUCCESS_COLOR = new Color(76, 175, 80); // Màu xanh lá mượt mà

    public MainFrame() {
        setTitle("VNUHCM-US :: Java Mail Client");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sử dụng TabbedPane với thiết kế phẳng
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT); // Đặt Tab bên trái cho hiện đại

        // Thêm Icon vào Tabs (Cỡ 20)
        Icon sendIcon = FontIcon.of(FontAwesomeSolid.PAPER_PLANE, 20, Color.GRAY);
        Icon inboxIcon = FontIcon.of(FontAwesomeSolid.INBOX, 20, Color.GRAY);

        tabbedPane.addTab("Soạn Thư", sendIcon, createSendPanel());
        tabbedPane.addTab("Hộp Thư Đến", inboxIcon, createInboxPanel());

        // Tối ưu UI cho TabbedPane
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 50);

        add(tabbedPane);
    }

    // Panel Gửi Mail (Thiết kế lại bố cục)
    private JPanel createSendPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30)); // Khoảng trắng viền ngoài

        // Khu vực Header (Người nhận, Tiêu đề)
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách giữa các ô
        gbc.weightx = 0;

        // Ô "Đến"
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblTo = new JLabel("Đến:");
        lblTo.setIcon(FontIcon.of(FontAwesomeSolid.USER, 14, Color.GRAY)); // Icon nhỏ
        headerPanel.add(lblTo, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTo = new JTextField();
        txtTo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "example@gmail.com"); // Chữ mờ hướng dẫn
        headerPanel.add(txtTo, gbc);

        // Ô "Tiêu đề"
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblSub = new JLabel("Tiêu đề:");
        lblSub.setIcon(FontIcon.of(FontAwesomeSolid.TAG, 14, Color.GRAY));
        headerPanel.add(lblSub, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtSubject = new JTextField();
        headerPanel.add(txtSubject, gbc);

        // Khu vực Body
        txtBody = new JTextArea();
        txtBody.setLineWrap(true);
        txtBody.setWrapStyleWord(true);
        JScrollPane scrollBody = new JScrollPane(txtBody);
        scrollBody.setBorder(BorderFactory.createTitledBorder("Nội dung thư"));

        // Nút Gửi "bóng bẩy"
        JButton btnSend = new JButton("GỬI MAIL NGAY");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSend.setBackground(PRIMARY_COLOR);
        btnSend.setForeground(Color.WHITE);
        // Thêm Icon gửi cỡ 20 màu trắng
        btnSend.setIcon(FontIcon.of(FontAwesomeSolid.PAPER_PLANE, 20, Color.WHITE));
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hiện bàn tay khi rê chuột

        // Làm tròn góc nút bấm (FlatLaf property)
        btnSend.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnSend.addActionListener(e -> handleSendMail());

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollBody, BorderLayout.CENTER);
        panel.add(btnSend, BorderLayout.SOUTH);

        return panel;
    }

    // Panel Nhận Mail (Thiết kế lại)
    private JPanel createInboxPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        txtInboxLog = new JTextArea();
        txtInboxLog.setEditable(false);
        txtInboxLog.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Font code dễ đọc log
        txtInboxLog.setBackground(new Color(248, 249, 250)); // Màu nền xám cực nhẹ
        JScrollPane scrollInbox = new JScrollPane(txtInboxLog);
        scrollInbox.setBorder(BorderFactory.createTitledBorder("Nhật ký hộp thư (POP3)"));

        JButton btnRefresh = new JButton("LÀM MỚI");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(SUCCESS_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        // Icon xoay vòng cỡ 18
        btnRefresh.setIcon(FontIcon.of(FontAwesomeSolid.SYNC, 18, Color.WHITE));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnRefresh.addActionListener(e -> handleRefreshInbox());

        panel.add(scrollInbox, BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }

    // Logic SMTP (Giữ nguyên luồng Thread của Tam)
    private void handleSendMail() {
        // ... (Giữ nguyên logic kiểm tra email@ và Thread cũ)...
        String to = txtTo.getText();
        String subject = txtSubject.getText();
        String body = txtBody.getText();

        if (!to.contains("@") || !to.contains(".")) {
            JOptionPane.showMessageDialog(this, "Địa chỉ email không hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                smtpService.send(new EmailContent(to, subject, body));
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Đã gửi mail thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    // Tự động xóa trắng ô nhập liệu sau khi gửi xong
                    txtTo.setText(""); txtSubject.setText(""); txtBody.setText("");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi gửi mail: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    // Logic POP3 (Giữ nguyên luồng Thread cũ)
    private void handleRefreshInbox() {
        txtInboxLog.setText("Đang kết nối đến POP3 Server... Vui lòng đợi.\n");
        new Thread(() -> {
            String result = pop3Service.receive();
            SwingUtilities.invokeLater(() -> txtInboxLog.setText(result));
        }).start();
    }

    public static void setupLook() {
        try {
            // Chuyển sang chủ đề sáng hiện đại (IntelliJ Style)
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
    }
}