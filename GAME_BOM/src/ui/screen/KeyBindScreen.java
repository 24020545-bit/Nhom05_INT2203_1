
package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public class KeyBindScreen extends JPanel {

    private Image backgroundImage;
    private JButton backBtn;

    public KeyBindScreen(GameFrame frame) {

        // Đọc ảnh nền
        java.net.URL imageURL =
                getClass().getResource("/Screen/KeyBindScreen.png");

        if (imageURL != null) {
            backgroundImage = new ImageIcon(imageURL).getImage();
        } else {
            System.out.println("Không tìm thấy KeyBindScreen.png");
        }

        // Sử dụng null layout để đặt nút đúng vị trí trên ảnh
        setLayout(null);

        // Tạo nút quay lại
        backBtn = new JButton();

        // Nút trong suốt vì hình ảnh đã có sẵn nút Quay lại
        backBtn.setOpaque(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);

        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Quay lại màn hình chính
        backBtn.addActionListener(e ->
                frame.showScreen("HOME"));

        add(backBtn);
    }

    // Căn chỉnh nút theo kích thước màn hình
    @Override
    public void doLayout() {

        int w = getWidth();
        int h = getHeight();

        // Vị trí nút Quay lại trên ảnh nền
        backBtn.setBounds(
                (int) (w * 0.40),
                (int) (h * 0.84),
                (int) (w * 0.20),
                (int) (h * 0.12)
        );
    }

    // Vẽ ảnh nền
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (backgroundImage != null) {

            Graphics2D g2d = (Graphics2D) g.create();

            // Chất lượng hiển thị ảnh
            g2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            // Vẽ ảnh phủ toàn bộ JPanel
            g2d.drawImage(
                    backgroundImage,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
            );

            g2d.dispose();
        }
    }
}
