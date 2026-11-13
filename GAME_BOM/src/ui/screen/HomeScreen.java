
package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JPanel {

    private Image backgroundImage;

    public HomeScreen(GameFrame frame) {

        // Đọc ảnh nền từ thư mục resources
        java.net.URL imageURL =
                getClass().getResource("/Screen/HomeScreen.png");

        if (imageURL != null) {
            backgroundImage = new ImageIcon(imageURL).getImage();
        } else {
            System.out.println("Không tìm thấy HomeScreen.png");
        }

        // Sử dụng GridBagLayout để căn chỉnh các nút
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 0, 12, 0);

        // Nút bắt đầu
        JButton startBtn = createButton("BẮT ĐẦU");

        // Nút thiết lập phím
        JButton keyBindBtn = createButton("THIẾT LẬP PHÍM");

        // Xử lý sự kiện
        startBtn.addActionListener(e ->
                frame.showScreen("GAME_SETTING"));

        keyBindBtn.addActionListener(e ->
                frame.showScreen("KEY_BIND"));

        // Đẩy các nút xuống phía dưới màn hình
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(0, 0, 10, 0);

        add(startBtn, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 75, 0);

        add(keyBindBtn, gbc);
    }

    // Tạo giao diện nút
    private JButton createButton(String text) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(260, 55));

        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(220, 65, 35));

        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(
                new Color(255, 190, 70), 3));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng khi di chuột vào nút
        btn.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(255, 130, 35));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(220, 65, 35));
            }
        });

        return btn;
    }

    // Vẽ ảnh nền
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (backgroundImage != null) {

            Graphics2D g2d = (Graphics2D) g.create();

            // Làm ảnh mượt khi phóng to, thu nhỏ
            g2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            // Vẽ ảnh phủ toàn bộ màn hình
            g2d.drawImage(
                    backgroundImage,
                    0, 0,
                    getWidth(),
                    getHeight(),
                    this
            );

            g2d.dispose();
        }
    }
}
