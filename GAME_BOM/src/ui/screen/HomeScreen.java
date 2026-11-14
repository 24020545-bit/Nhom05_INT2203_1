
package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JPanel {

    private Image backgroundImage;

    private JButton startBtn;
    private JButton keyBindBtn;

    // Kích thước màn hình thiết kế ban đầu
    private final int BASE_WIDTH = 1024;
    private final int BASE_HEIGHT = 576;

    public HomeScreen(GameFrame frame) {

        // Đọc ảnh nền
        java.net.URL imageURL =
                getClass().getResource("/Screen/HomeScreen.png");

        if (imageURL != null) {
            backgroundImage = new ImageIcon(imageURL).getImage();
        } else {
            System.out.println("Không tìm thấy HomeScreen.png");
        }

        setLayout(null);
        setOpaque(false);

        // Tạo nút
        startBtn = createButton("BẮT ĐẦU");
        keyBindBtn = createButton("THIẾT LẬP PHÍM");

        // Xử lý sự kiện
        startBtn.addActionListener(e ->
                frame.showScreen("GAME_SETTING"));

        keyBindBtn.addActionListener(e ->
                frame.showScreen("KEY_BIND"));

        add(startBtn);
        add(keyBindBtn);

        // Tự động cập nhật vị trí khi thay đổi kích thước màn hình
        addComponentListener(new java.awt.event.ComponentAdapter() {

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateButtonPosition();
            }
        });
    }

    // Cập nhật vị trí và kích thước nút
    private void updateButtonPosition() {

        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;

        // Tọa độ và kích thước nút ban đầu
        int buttonWidth = (int) (260 * scaleX);
        int buttonHeight = (int) (55 * scaleY);

        int x = (int) (380 * scaleX);

        int startY = (int) (300 * scaleY);
        int keyBindY = (int) (375 * scaleY);

        // Cập nhật vị trí nút
        startBtn.setBounds(x, startY, buttonWidth, buttonHeight);
        keyBindBtn.setBounds(x, keyBindY, buttonWidth, buttonHeight);

        // Cập nhật cỡ chữ
        int fontSize = Math.max(12, (int) (20 * Math.min(scaleX, scaleY)));

        startBtn.setFont(new Font("Arial", Font.BOLD, fontSize));
        keyBindBtn.setFont(new Font("Arial", Font.BOLD, fontSize));

        repaint();
    }

    // Tạo giao diện nút
    private JButton createButton(String text) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Arial", Font.BOLD, 20));

        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(220, 65, 35));

        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createLineBorder(
                new Color(255, 190, 70), 3));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng khi di chuột
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

            g2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

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
