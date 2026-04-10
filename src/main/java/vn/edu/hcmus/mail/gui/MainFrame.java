package vn.edu.hcmus.mail.gui;

import com.formdev.flatlaf.FlatLightLaf;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTextField txtTo, txtSubject;
    private JTextArea txtBody, txtInboxLog;
    private SmtpService smtpService = new SmtpService();
    private Pop3Service pop3Service = new Pop3Service();

    public MainFrame() {
        setTitle("Java Mail Client - VNUHCM-US");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sử dụng TabbedPane để chia khu vực Gửi và Nhận
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Soạn Thư (SMTP)", createSendPanel());
        tabbedPane.addTab("Hộp Thư Đến (POP3)", createInboxPanel());

        add(tabbedPane);
    }

    // Panel Gửi Mail
    private JPanel createSendPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new GridLayout(2, 2, 5, 5));
        header.add(new JLabel("Đến:"));
        txtTo = new JTextField();
        header.add(txtTo);
        header.add(new JLabel("Tiêu đề:"));
        txtSubject = new JTextField();
        header.add(txtSubject);

        txtBody = new JTextArea();
        txtBody.setBorder(BorderFactory.createTitledBorder("Nội dung thư"));

        JButton btnSend = new JButton("GỬI MAIL");
        btnSend.setBackground(new Color(0, 120, 215));
        btnSend.setForeground(Color.WHITE);

        btnSend.addActionListener(e -> handleSendMail());

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtBody), BorderLayout.CENTER);
        panel.add(btnSend, BorderLayout.SOUTH);

        return panel;
    }

    // Panel Nhận Mail
    private JPanel createInboxPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtInboxLog = new JTextArea();
        txtInboxLog.setEditable(false);
        txtInboxLog.setBackground(new Color(245, 245, 245));

        JButton btnRefresh = new JButton("LÀM MỚI HỘP THƯ");
        btnRefresh.addActionListener(e -> handleRefreshInbox());

        panel.add(new JScrollPane(txtInboxLog), BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }

    // Logic xử lý Gửi (Chạy trên luồng riêng để không đơ giao diện)
    private void handleSendMail() {
        String to = txtTo.getText();
        String subject = txtSubject.getText();
        String body = txtBody.getText();

        new Thread(() -> {
            try {
                smtpService.send(new EmailContent(to, subject, body));
                JOptionPane.showMessageDialog(this, "Đã gửi mail thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi gửi mail: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    // Logic xử lý Nhận (Tạm thời in log ra giao diện)
    private void handleRefreshInbox() {
        txtInboxLog.setText("Đang kiểm tra thư mới... Vui lòng đợi.\n");

        new Thread(() -> {
            // Lấy dữ liệu từ Service
            String result = pop3Service.receive();

            // Cập nhật giao diện phải chạy trong SwingUtilities để tránh lỗi đồng bộ
            javax.swing.SwingUtilities.invokeLater(() -> {
                txtInboxLog.setText(result); // Hiển thị nội dung mail lên ô trắng
            });
        }).start();
    }

    public static void setupLook() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
    }
}