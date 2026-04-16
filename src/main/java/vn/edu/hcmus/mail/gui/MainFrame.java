package vn.edu.hcmus.mail.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.database.EmailCache;
import vn.edu.hcmus.mail.supabase.SupabaseEmailParser;
import vn.edu.hcmus.mail.supabase.SupabaseSyncService;
import vn.edu.hcmus.mail.model.Email;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private JTextField txtTo, txtSubject;
    private JTextArea txtBody, txtInboxLog;
    private SmtpService smtpService = new SmtpService();
    private Pop3Service pop3Service = new Pop3Service();
    private EmailCache emailCache;

    // Quản lý file đính kèm
    private DefaultListModel<File> attachmentModel = new DefaultListModel<>();
    private JList<File> listAttachments = new JList<>(attachmentModel);

    // History components
    private DefaultListModel<Email> sentHistoryModel = new DefaultListModel<>();
    private DefaultListModel<Email> receivedHistoryModel = new DefaultListModel<>();
    private JList<Email> listSentHistory = new JList<>(sentHistoryModel);
    private JList<Email> listReceivedHistory = new JList<>(receivedHistoryModel);
    private JTextField txtSearchSent = new JTextField();
    private JTextField txtSearchReceived = new JTextField();
    private JLabel lblUnreadCount = new JLabel();
    private JButton btnPullSupabase;

    private final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private final Color DANGER_COLOR = new Color(244, 67, 54);
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MainFrame() {
        setTitle("VNUHCM-US :: Java Mail Client");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize database cache
        emailCache = EmailCache.getInstance();
        loadHistoryData();

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        tabbedPane.addTab("Soạn Thư", createSendPanel());
        tabbedPane.addTab("Hộp Thư Đến", createInboxPanel());
        tabbedPane.addTab("Đã Gửi", createSentHistoryPanel());
        tabbedPane.addTab("Lịch Sử", createHistoryPanel());
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 50);

        add(tabbedPane);
    }

    private void loadHistoryData() {
        sentHistoryModel.clear();
        receivedHistoryModel.clear();
        emailCache.getAllSentEmails().forEach(sentHistoryModel::addElement);
        emailCache.getAllReceivedEmails().forEach(receivedHistoryModel::addElement);
        updateUnreadCount();
    }

    private void updateUnreadCount() {
        int count = emailCache.getUnreadCount();
        lblUnreadCount.setText("Chưa đọc: " + count);
        lblUnreadCount.setForeground(count > 0 ? DANGER_COLOR : Color.GRAY);
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
        lblTo.setIcon(null); // FontIcon.of(FontAwesomeSolid.USERS, 14, Color.GRAY));
        headerPanel.add(lblTo, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTo = new JTextField();
        txtTo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "mail1@gmail.com mail2@gmail.com (cách nhau bằng dấu cách)");
        headerPanel.add(txtTo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblSub = new JLabel("Tiêu đề:");
        lblSub.setIcon(null); // FontIcon.of(FontAwesomeSolid.TAG, 14, Color.GRAY));
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
        btnAddFile.setIcon(null); // FontIcon.of(FontAwesomeSolid.PAPERCLIP, 14, PRIMARY_COLOR));

        JButton btnRemoveFile = new JButton("Xóa tệp");
        btnRemoveFile.setIcon(null); // FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));

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
        btnSend.setIcon(null); // FontIcon.of(FontAwesomeSolid.PAPER_PLANE, 18, Color.WHITE));
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
        btnRefresh.setIcon(null); // FontIcon.of(FontAwesomeSolid.SYNC_ALT, 18, Color.WHITE));
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

                    SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Đang gửi hàng loạt tới " + recipients.size() + " người...");
                            loadHistoryData();
                    });
                } else {
                    // GỬI ĐƠN LẺ + CÓ ĐÍNH KÈM
                    EmailContent email = new EmailContent(recipients.get(0), subject, body);
                    email.setAttachmentPaths(selectedFiles);
                    smtpService.send(email);

                    SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Đã gửi mail thành công!");
                            loadHistoryData();
                    });
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
            SwingUtilities.invokeLater(() -> {
                txtInboxLog.setText(result);
                loadHistoryData();
            });
        }).start();
    }

    private JPanel createSentHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        JLabel titleLabel = new JLabel("Lịch sử email đã gửi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        txtSearchSent.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm email đã gửi...");
        JButton btnSearchSent = new JButton("Tìm");
        btnSearchSent.setIcon(null); // FontIcon.of(FontAwesomeSolid.SEARCH, 14, Color.GRAY));
        searchPanel.add(txtSearchSent, BorderLayout.CENTER);
        searchPanel.add(btnSearchSent, BorderLayout.EAST);
        headerPanel.add(searchPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        listSentHistory.setCellRenderer(new EmailListCellRenderer());
        JScrollPane scrollPane = new JScrollPane(listSentHistory);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnViewSent = new JButton("Xem chi tiết");
        btnViewSent.setIcon(null); // FontIcon.of(FontAwesomeSolid.EYE, 14, PRIMARY_COLOR));
        JButton btnDeleteSent = new JButton("Xóa");
        btnDeleteSent.setIcon(null); // FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));
        JButton btnRefreshSent = new JButton("Làm mới");
        btnRefreshSent.setIcon(null); // FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, SUCCESS_COLOR));

        btnPanel.add(btnViewSent);
        btnPanel.add(btnDeleteSent);
        btnPanel.add(btnRefreshSent);
        panel.add(btnPanel, BorderLayout.SOUTH);

        btnSearchSent.addActionListener(e -> searchSentEmails());
        btnViewSent.addActionListener(e -> viewSentEmailDetail());
        btnDeleteSent.addActionListener(e -> deleteSentEmail());
        btnRefreshSent.addActionListener(e -> {
            loadHistoryData();
            JOptionPane.showMessageDialog(this, "Đã làm mới lịch sử gửi!");
        });

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Lịch sử tất cả email");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        titlePanel.add(lblUnreadCount);
        topPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        txtSearchReceived.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm email...");
        JButton btnSearchReceived = new JButton("Tìm");
        btnSearchReceived.setIcon(null); // FontIcon.of(FontAwesomeSolid.SEARCH, 14, Color.GRAY));
        searchPanel.add(txtSearchReceived, BorderLayout.CENTER);
        searchPanel.add(btnSearchReceived, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        listReceivedHistory.setCellRenderer(new EmailListCellRenderer());
        JPanel receivedPanel = new JPanel(new BorderLayout());
        receivedPanel.add(new JLabel("Email nhận được:"), BorderLayout.NORTH);
        receivedPanel.add(new JScrollPane(listReceivedHistory), BorderLayout.CENTER);

        listSentHistory.setCellRenderer(new EmailListCellRenderer());
        JPanel sentPanel = new JPanel(new BorderLayout());
        sentPanel.add(new JLabel("Email đã gửi:"), BorderLayout.NORTH);
        sentPanel.add(new JScrollPane(listSentHistory), BorderLayout.CENTER);

        splitPane.setTopComponent(receivedPanel);
        splitPane.setBottomComponent(sentPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnViewReceived = new JButton("Xem chi tiết nhận");
        btnViewReceived.setIcon(null); // FontIcon.of(FontAwesomeSolid.EYE, 14, PRIMARY_COLOR));
        JButton btnMarkRead = new JButton("Đánh dấu đã đọc");
        btnMarkRead.setIcon(null); // FontIcon.of(FontAwesomeSolid.CHECK, 14, SUCCESS_COLOR));
        JButton btnDeleteReceived = new JButton("Xóa nhận");
        btnDeleteReceived.setIcon(null); // FontIcon.of(FontAwesomeSolid.TRASH, 14, DANGER_COLOR));
        JButton btnRefreshHistory = new JButton("Làm mới");
        btnRefreshHistory.setIcon(null); // FontIcon.of(FontAwesomeSolid.SYNC_ALT, 14, SUCCESS_COLOR));
        JButton btnViewOffline = new JButton("Xem Offline");
        btnViewOffline.setIcon(null); // FontIcon.of(FontAwesomeSolid.WIFI, 14, Color.GRAY));
        JButton btnSyncSupabase = new JButton("Sync Supabase");
        btnSyncSupabase.setIcon(null); // FontIcon.of(FontAwesomeSolid.CLOUD_DOWNLOAD_ALT, 14, new Color(49, 201, 95)));
        JButton btnPullSupabase = new JButton("Pull từ Supabase");
        btnPullSupabase.setIcon(null); // FontIcon.of(FontAwesomeSolid.DOWNLOAD, 14, new Color(49, 201, 95)));

        btnPanel.add(btnViewReceived);
        btnPanel.add(btnMarkRead);
        btnPanel.add(btnDeleteReceived);
        btnPanel.add(btnRefreshHistory);
        btnPanel.add(btnViewOffline);
        btnPanel.add(btnSyncSupabase);
        btnPanel.add(btnPullSupabase);
        panel.add(btnPanel, BorderLayout.SOUTH);

        btnSearchReceived.addActionListener(e -> searchAllEmails());
        btnViewReceived.addActionListener(e -> viewReceivedEmailDetail());
        btnMarkRead.addActionListener(e -> markAsRead());
        btnDeleteReceived.addActionListener(e -> deleteReceivedEmail());
        btnRefreshHistory.addActionListener(e -> {
            loadHistoryData();
            JOptionPane.showMessageDialog(this, "Đã làm mới lịch sử!");
        });
        btnViewOffline.addActionListener(e -> {
            if (emailCache.isAvailableOffline()) {
                JOptionPane.showMessageDialog(this, "Có dữ liệu offline! Bạn có thể xem email cũ khi không có mạng.");
            } else {
                JOptionPane.showMessageDialog(this, "Chưa có dữ liệu offline. Vui lòng kết nối mạng để tải email.");
            }
        });
        btnSyncSupabase.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Email đã được tự động sync lên Supabase khi gửi/nhận!\nXem console để kiểm tra.");
        });
        btnPullSupabase.addActionListener(e -> pullFromSupabase());

        return panel;
    }

    private void pullFromSupabase() {
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    btnPullSupabase.setEnabled(false);
                    btnPullSupabase.setText("Đang tải...");
                });

                int addedCount = 0;

                String sentJson = SupabaseSyncService.getInstance().fetchFromSupabase("sent_emails");
                List<Email> sentEmails = SupabaseEmailParser.parseSentEmails(sentJson);
                for (Email email : sentEmails) {
                    if (emailCache.getSentEmail(email.getMsgId()).isEmpty()) {
                        emailCache.cacheSentEmail(email);
                        addedCount++;
                    }
                }

                String receivedJson = SupabaseSyncService.getInstance().fetchFromSupabase("received_emails");
                List<Email> receivedEmails = SupabaseEmailParser.parseReceivedEmails(receivedJson);
                for (Email email : receivedEmails) {
                    if (emailCache.getReceivedEmail(email.getMsgId()).isEmpty()) {
                        emailCache.cacheReceivedEmail(email);
                        addedCount++;
                    }
                }

                final int finalCount = addedCount;
                SwingUtilities.invokeLater(() -> {
                    loadHistoryData();
                    btnPullSupabase.setEnabled(true);
                    btnPullSupabase.setText("Pull từ Supabase");
                    JOptionPane.showMessageDialog(MainFrame.this, "Đã tải " + finalCount + " email từ Supabase!");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnPullSupabase.setEnabled(true);
                    btnPullSupabase.setText("Pull từ Supabase");
                    JOptionPane.showMessageDialog(MainFrame.this, "Lỗi Supabase: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void searchSentEmails() {
        String keyword = txtSearchSent.getText().trim();
        sentHistoryModel.clear();
        if (keyword.isEmpty()) {
            emailCache.getAllSentEmails().forEach(sentHistoryModel::addElement);
        } else {
            emailCache.searchSentEmails(keyword).forEach(sentHistoryModel::addElement);
        }
    }

    private void searchAllEmails() {
        String keyword = txtSearchReceived.getText().trim();
        sentHistoryModel.clear();
        receivedHistoryModel.clear();
        if (keyword.isEmpty()) {
            emailCache.getAllSentEmails().forEach(sentHistoryModel::addElement);
            emailCache.getAllReceivedEmails().forEach(receivedHistoryModel::addElement);
        } else {
            emailCache.searchSentEmails(keyword).forEach(sentHistoryModel::addElement);
            emailCache.searchReceivedEmails(keyword).forEach(receivedHistoryModel::addElement);
        }
    }

    private void viewSentEmailDetail() {
        Email email = listSentHistory.getSelectedValue();
        if (email == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một email!");
            return;
        }
        showEmailDetail(email, "Email đã gửi");
    }

    private void viewReceivedEmailDetail() {
        Email email = listReceivedHistory.getSelectedValue();
        if (email == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một email!");
            return;
        }
        showEmailDetail(email, "Email nhận được");
        if (!email.isRead()) {
            emailCache.markReceivedAsRead(email.getMsgId());
            loadHistoryData();
        }
    }

    private void showEmailDetail(Email email, String title) {
        StringBuilder detail = new StringBuilder();
        detail.append("=== ").append(title).append(" ===\n\n");
        detail.append("Từ: ").append(email.getFromEmail()).append("\n");
        detail.append("Đến: ").append(email.getToEmail()).append("\n");
        detail.append("Tiêu đề: ").append(email.getSubject()).append("\n");
        detail.append("Thời gian: ").append(email.getTimestamp().format(DATE_FORMAT)).append("\n");
        if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
            detail.append("Đính kèm: ").append(String.join(", ", email.getAttachments())).append("\n");
        }
        detail.append("\n--- Nội dung ---\n");
        detail.append(email.getBody());

        JTextArea textArea = new JTextArea(detail.toString());
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSentEmail() {
        Email email = listSentHistory.getSelectedValue();
        if (email == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một email!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa email này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            emailCache.removeSentEmail(email.getMsgId());
            loadHistoryData();
            JOptionPane.showMessageDialog(this, "Đã xóa email!");
        }
    }

    private void deleteReceivedEmail() {
        Email email = listReceivedHistory.getSelectedValue();
        if (email == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một email!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa email này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            emailCache.removeReceivedEmail(email.getMsgId());
            loadHistoryData();
            JOptionPane.showMessageDialog(this, "Đã xóa email!");
        }
    }

    private void markAsRead() {
        Email email = listReceivedHistory.getSelectedValue();
        if (email == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một email!");
            return;
        }
        emailCache.markReceivedAsRead(email.getMsgId());
        loadHistoryData();
        JOptionPane.showMessageDialog(this, "Đã đánh dấu là đã đọc!");
    }

    static class EmailListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Email) {
                Email email = (Email) value;
                String text = String.format("<html><b>%s</b> - %s<br><font size=2 color=gray>%s | %s</font></html>",
                    email.getSubject() != null ? email.getSubject() : "(Không có tiêu đề)",
                    email.getType() == Email.EmailType.SENT ? "→ " + email.getToEmail() : "← " + email.getFromEmail(),
                    email.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    email.isRead() ? "" : "<font color=red>●</font>"
                );
                setText(text);
            }
            return this;
        }
    }

    public static void setupLook() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            JLabel.class.getResource("/icons/");
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        System.setProperty("flatlaf.useWindowDecorations", "false");
    }
}