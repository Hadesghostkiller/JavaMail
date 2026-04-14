package vn.edu.hcmus.mail.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;
import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MainFrame extends JFrame {
    private JTextField txtTo, txtSubject;
    private JTextArea txtBody, txtInboxLog;
    private SmtpService smtpService = new SmtpService();
    private Pop3Service pop3Service = new Pop3Service();

    // Quản lý file đính kèm
    private DefaultListModel<File> attachmentModel = new DefaultListModel<>();
    private JList<File> listAttachments = new JList<>(attachmentModel);

    private final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private final Color DANGER_COLOR = new Color(244, 67, 54);

    public MainFrame() {
        setTitle("VNUHCM-US :: Java Mail Client");
        setSize(1000, 750); // Tăng nhẹ kích thước để chứa list file
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        Icon sendIcon = FontIcon.of(FontAwesomeSolid.PAPER_PLANE, 20, Color.GRAY);
        Icon inboxIcon = FontIcon.of(FontAwesomeSolid.INBOX, 20, Color.GRAY);

        tabbedPane.addTab("Soạn Thư", sendIcon, createSendPanel());
        tabbedPane.addTab("Hộp Thư Đến", inboxIcon, createInboxPanel());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 50);

        add(tabbedPane);
    }

    private JPanel createSendPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. Header: To, Subject
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTo = new JLabel("Đến:");
        lblTo.setIcon(FontIcon.of(FontAwesomeSolid.USERS, 14, Color.GRAY));
        headerPanel.add(lblTo, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTo = new JTextField();
        txtTo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "mail1@gmail.com mail2@gmail.com (cách nhau bằng dấu cách)");
        headerPanel.add(txtTo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblSub = new JLabel("Tiêu đề:");
        lblSub.setIcon(FontIcon.of(FontAwesomeSolid.TAG, 14, Color.GRAY));
        headerPanel.add(lblSub, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtSubject = new JTextField();
        headerPanel.add(txtSubject, gbc);

        // 2. Body: Nội dung text
        txtBody = new JTextArea();
        txtBody.setLineWrap(true);
        txtBody.setWrapStyleWord(true);
        JScrollPane scrollBody = new JScrollPane(txtBody);
        scrollBody.setBorder(BorderFactory.createTitledBorder("Nội dung thư"));

        // 3. Attachment: Khu vực chọn file
        JPanel attachPanel = new JPanel(new BorderLayout(5, 5));
        attachPanel.setPreferredSize(new Dimension(0, 120));

        listAttachments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollFiles = new JScrollPane(listAttachments);
        scrollFiles.setBorder(BorderFactory.createTitledBorder("Tệp đính kèm"));

        JPanel attachButtons = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton btnAddFile = new JButton("Thêm tệp");
        btnAddFile.setIcon(FontIcon.of(FontAwesomeSolid.PAPERCLIP, 14, PRIMARY_COLOR));

        JButton btnRemoveFile = new JButton("Xóa tệp");
        btnRemoveFile.setIcon(FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));

        attachButtons.add(btnAddFile);
        attachButtons.add(btnRemoveFile);
        attachPanel.add(scrollFiles, BorderLayout.CENTER);
        attachPanel.add(attachButtons, BorderLayout.EAST);

        // Logic nút chọn file
        btnAddFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            ImagePreviewer previewer = new ImagePreviewer(chooser);
            chooser.setAccessory(previewer);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                for (File f : chooser.getSelectedFiles()) {
                    if (f.length() > 25 * 1024 * 1024) {
                        JOptionPane.showMessageDialog(this, "File " + f.getName() + " quá 25MB!");
                        continue;
                    }
                    if (!attachmentModel.contains(f)) attachmentModel.addElement(f);
                }
            }
        });

        btnRemoveFile.addActionListener(e -> {
            int index = listAttachments.getSelectedIndex();
            if (index != -1) attachmentModel.remove(index);
        });

        // 4. Footer: Nút Gửi
        JButton btnSend = new JButton("GỬI MAIL");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSend.setBackground(PRIMARY_COLOR);
        btnSend.setForeground(Color.WHITE);
        btnSend.setIcon(FontIcon.of(FontAwesomeSolid.PAPER_PLANE, 18, Color.WHITE));
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnSend.addActionListener(e -> handleSendMail());

        // Ghép các phần lại
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(scrollBody, BorderLayout.CENTER);
        centerPanel.add(attachPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(btnSend, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInboxPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        txtInboxLog = new JTextArea();
        txtInboxLog.setEditable(false);
        txtInboxLog.setBackground(new Color(248, 249, 250));
        JScrollPane scrollInbox = new JScrollPane(txtInboxLog);
        scrollInbox.setBorder(BorderFactory.createTitledBorder("Nhật ký hộp thư (POP3)"));

        JButton btnRefresh = new JButton("LÀM MỚI");
        btnRefresh.setBackground(SUCCESS_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setIcon(FontIcon.of(FontAwesomeSolid.SYNC_ALT, 18, Color.WHITE));
        btnRefresh.addActionListener(e -> handleRefreshInbox());

        panel.add(scrollInbox, BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);
        return panel;
    }

    private void handleSendMail() {
        // 1. Lấy và chuẩn hóa danh sách người nhận (ngăn cách bằng dấu cách)
        String rawTo = txtTo.getText().trim().replaceAll("\\s+", " ");
        String subject = txtSubject.getText();
        String body = txtBody.getText();

        if (rawTo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập người nhận!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Tách chuỗi thành List các email
        List<String> recipients = Arrays.asList(rawTo.split(" "));

        // 3. QUAN TRỌNG: Lấy danh sách file THẬT từ Giao diện (listAttachments)
        // Thay thế hoàn toàn việc code cứng đường dẫn!
        List<String> selectedFiles = new ArrayList<>();
        for (int i = 0; i < attachmentModel.size(); i++) {
            File file = attachmentModel.getElementAt(i);
            selectedFiles.add(file.getAbsolutePath());
        }

        // 4. Bắt đầu tiến trình gửi
        new Thread(() -> {
            try {
                if (recipients.size() > 1) {
                    // GỬI HÀNG LOẠT + CÓ ĐÍNH KÈM
                    smtpService.send_Bulk(recipients, subject, body, selectedFiles);

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Đang gửi hàng loạt tới " + recipients.size() + " người...")
                    );
                } else {
                    // GỬI ĐƠN LẺ + CÓ ĐÍNH KÈM
                    EmailContent email = new EmailContent(recipients.get(0), subject, body);
                    email.setAttachmentPaths(selectedFiles);
                    smtpService.send(email);

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Đã gửi mail thành công!")
                    );
                }

                // Xóa trắng giao diện sau khi gửi thành công để người dùng làm việc tiếp
                SwingUtilities.invokeLater(() -> {
                    txtTo.setText(""); txtSubject.setText(""); txtBody.setText("");
                    attachmentModel.clear(); // Xóa sạch danh sách file đã chọn
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    // Helper tạo cấu hình cho Bulk Send
    private Properties prepareSmtpProps() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(MailConfig.SMTP_PORT));
        return props;
    }

    private void handleRefreshInbox() {
        txtInboxLog.setText("Đang kết nối đến POP3 Server...\n");
        new Thread(() -> {
            String result = pop3Service.receive();
            SwingUtilities.invokeLater(() -> txtInboxLog.setText(result));
        }).start();
    }

    public static void setupLook() {
        try { UIManager.setLookAndFeel(new FlatIntelliJLaf()); }
        catch (Exception ex) { System.err.println("Failed to initialize LaF"); }
    }
}