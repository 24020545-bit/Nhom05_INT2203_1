
package ui.screen;

import ui.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GameSettingScreen extends JPanel {

    // Kích thước màn hình thiết kế ban đầu
    private static final int BASE_WIDTH = 1024;
    private static final int BASE_HEIGHT = 576;

    private final GameFrame frame;
    private BufferedImage backgroundImage;

    // Số người chơi đang được chọn
    private int selectedPlayers = 1;

    // Các nút
    private final JButton player1Btn;
    private final JButton player2Btn;
    private final JButton nextBtn;
    private final JButton backBtn;

    // Các khu vực giao diện
    private final JPanel gridPanel;
    private final JLayeredPane layeredPane;

    private final GridLayout gridLayout;

    public GameSettingScreen(GameFrame frame) {

        this.frame = frame;

        // Đọc ảnh nền
        try {
            backgroundImage = ImageIO.read(
                    getClass().getResource("/Screen/GameSettingScreen.png")
            );
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        // 1. Tạo lớp chứa các thành phần giao diện
        layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);

        add(layeredPane, BorderLayout.CENTER);

        // 2. Tạo bảng chọn số người chơi
        gridLayout = new GridLayout(2, 1, 0, 10);

        gridPanel = new JPanel(gridLayout);
        gridPanel.setOpaque(false);

        player1Btn = createPlayerButton("1 NGƯỜI CHƠI");
        player2Btn = createPlayerButton("2 NGƯỜI CHƠI");

        gridPanel.add(player1Btn);
        gridPanel.add(player2Btn);

        // Đưa bảng chọn vào lớp mặc định
        layeredPane.add(gridPanel, JLayeredPane.DEFAULT_LAYER);

        // 3. Tạo nút Tiếp tục
        nextBtn = createImageButton();

        // Đưa nút lên lớp phía trên
        layeredPane.add(nextBtn, JLayeredPane.PALETTE_LAYER);

        // 4. Tạo nút Quay lại
        backBtn = createImageButton();

        // Đưa nút lên lớp phía trên
        layeredPane.add(backBtn, JLayeredPane.PALETTE_LAYER);

        // 5. Xử lý sự kiện chọn số người chơi
        player1Btn.addActionListener(e -> {
            selectedPlayers = 1;
            updatePlayerSelection();
        });

        player2Btn.addActionListener(e -> {
            selectedPlayers = 2;
            updatePlayerSelection();
        });

        // 6. Chuyển sang màn hình chọn bản đồ
        nextBtn.addActionListener(e -> {
            frame.setPlayerCount(selectedPlayers);
            frame.showScreen("LEVEL_SELECT");
        });

        // 7. Quay lại màn hình chính
        backBtn.addActionListener(e -> {
            frame.showScreen("HOME");
        });

        updatePlayerSelection();

        // 8. Co giãn giao diện khi thay đổi kích thước màn hình
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                scalePlayerGrid();
            }
        });
    }

    // Tạo nút chọn người chơi bo góc
    private JButton createPlayerButton(String text) {

        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                boolean selected = Boolean.TRUE.equals(
                        getClientProperty("selected")
                );

                // Màu nền của nút
                if (selected) {
                    g2.setColor(new Color(255, 205, 50));
                } else {
                    g2.setColor(new Color(20, 45, 110));
                }

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        25, 25
                );

                // Vẽ viền nút
                g2.setColor(new Color(120, 190, 255));
                g2.setStroke(new BasicStroke(3));

                g2.drawRoundRect(
                        1, 1,
                        getWidth() - 3,
                        getHeight() - 3,
                        25, 25
                );

                // Vẽ chữ trên nút
                g2.setFont(new Font("Arial", Font.BOLD, 20));

                if (selected) {
                    g2.setColor(new Color(20, 45, 110));
                } else {
                    g2.setColor(Color.WHITE);
                }

                FontMetrics fm = g2.getFontMetrics();

                int textX = (getWidth() - fm.stringWidth(getText())) / 2;

                int textY = (getHeight() - fm.getHeight()) / 2
                        + fm.getAscent();

                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // Tạo nút trong suốt để nhận sự kiện bấm trên ảnh nền
    private JButton createImageButton() {

        JButton btn = new JButton();

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // Cập nhật màu sắc nút đang được chọn
    private void updatePlayerSelection() {

        player1Btn.putClientProperty(
                "selected", selectedPlayers == 1
        );

        player2Btn.putClientProperty(
                "selected", selectedPlayers == 2
        );

        player1Btn.repaint();
        player2Btn.repaint();
    }

    // Co giãn bảng chọn và hai nút điều hướng
    private void scalePlayerGrid() {

        if (layeredPane.getWidth() <= 0 ||
                layeredPane.getHeight() <= 0) {
            return;
        }

        // Tính tỷ lệ co giãn theo kích thước màn hình
        double scaleX = (double) layeredPane.getWidth() / BASE_WIDTH;
        double scaleY = (double) layeredPane.getHeight() / BASE_HEIGHT;

        // =========================================
        // 1. CO GIÃN BẢNG CHỌN NGƯỜI CHƠI
        // =========================================

        // Điều chỉnh khoảng cách giữa hai ô chọn
        gridLayout.setHgap(0);

        gridLayout.setVgap(
                (int) Math.round(10 * scaleY)
        );

        // Tạo khoảng trống xung quanh bảng chọn
        gridPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        (int) Math.round(195 * scaleY),  // Khoảng cách phía trên
                        (int) Math.round(410 * scaleX),  // Khoảng cách bên trái
                        (int) Math.round(275 * scaleY),  // Khoảng cách phía dưới
                        (int) Math.round(210 * scaleX)   // Khoảng cách bên phải
                )
        );

        // Cho bảng chọn chiếm toàn bộ khu vực chứa
        gridPanel.setBounds(
                0,
                0,
                layeredPane.getWidth(),
                layeredPane.getHeight()
        );

        // =========================================
        // 2. CO GIÃN NÚT TIẾP TỤC
        // =========================================

        nextBtn.setBounds(
                (int) Math.round(350 * scaleX),
                (int) Math.round(345 * scaleY),
                (int) Math.round(325 * scaleX),
                (int) Math.round(82 * scaleY)
        );

        // =========================================
        // 3. CO GIÃN NÚT QUAY LẠI
        // =========================================

        backBtn.setBounds(
                (int) Math.round(380 * scaleX),
                (int) Math.round(442 * scaleY),
                (int) Math.round(265 * scaleX),
                (int) Math.round(77 * scaleY)
        );

        revalidate();
        repaint();
    }

    // Vẽ ảnh nền
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (backgroundImage != null) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.drawImage(
                    backgroundImage,
                    0, 0,
                    getWidth(), getHeight(),
                    this
            );

            g2.dispose();
        }
    }
}
