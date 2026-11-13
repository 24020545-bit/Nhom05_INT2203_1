package ui.screen;

import ui.GameFrame;
import utils.SaveManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LevelSelectScreen extends JPanel {
    private static final int TOTAL_LEVELS = 6;
    private static int unlockedLevel = 3; // Ví dụ: đang mở khóa tới cửa ải 3

    // Kích thước chuẩn ban đầu bạn đã căn chỉnh khớp với ảnh
    private static final int BASE_WIDTH = 1024;
    private static final int BASE_HEIGHT = 576;

    private final GameFrame parentFrame;
    private Image backgroundImage;
    private final JPanel topSpacer;
    private final JPanel gridPanel;
    private final GridLayout gridLayout;
    private final JPanel bottomPanel;
    private final JButton backBtn;

    public LevelSelectScreen(GameFrame frame) {
        this.parentFrame = frame;
        // Giữ nguyên BorderLayout cũ của bạn
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        loadBackgroundImage("/Screen/LevelSelectScreen.png", "resources/Screen/LevelSelectScreen.png");

        // 1. PHẦN TRÊN: Khoảng trống chừa cho chữ "CHỌN CỬA ẢI"
        topSpacer = new JPanel();
        topSpacer.setOpaque(false);
        topSpacer.setPreferredSize(new Dimension(0, 144));
        add(topSpacer, BorderLayout.NORTH);

        // 2. PHẦN GIỮA: Bảng lưới 2 hàng x 3 cột
        gridLayout = new GridLayout(2, 3, 47, 25);
        gridPanel = new JPanel(gridLayout);
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 167, 13, 179));

        buildLevelButtons();
        add(gridPanel, BorderLayout.CENTER);

        // 3. PHẦN DƯỚI: Vùng chứa nút "Quay lại Cài đặt" vô hình đè lên ảnh nền
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(0, 95));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        backBtn = createTransparentBackButton();
        backBtn.addActionListener(e -> frame.showScreen("GAME_SETTING"));
        bottomPanel.add(backBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // Tự động nhân tỷ lệ cho các thông số BorderLayout cũ khi phóng to / thu nhỏ cửa sổ
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleLayoutMargins();
            }
        });
    }

    /**
     * Giữ nguyên công thức căn chỉnh của bạn nhưng nhân thêm tỷ lệ co giãn của cửa sổ
     */
    private void scaleLayoutMargins() {
        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;

        // 1. Co giãn phần trên (144px gốc bạn đã căn)
        topSpacer.setPreferredSize(new Dimension(0, (int) Math.round(144 * scaleY)));

        // 2. Co giãn khoảng cách giữa các ô (47, 25 gốc) và viền bảng (15, 167, 13, 179 gốc bạn đã căn)
        gridLayout.setHgap((int) Math.round(47 * scaleX));
        gridLayout.setVgap((int) Math.round(25 * scaleY));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(
                (int) Math.round(15 * scaleY),
                (int) Math.round(167 * scaleX),
                (int) Math.round(13 * scaleY),
                (int) Math.round(179 * scaleX)
        ));

        // 3. Co giãn phần dưới (95px gốc) và vùng bấm nút Quay lại vô hình (310x60 khớp nút trên ảnh)
        bottomPanel.setPreferredSize(new Dimension(0, (int) Math.round(95 * scaleY)));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder((int) Math.round(10 * scaleY), 0, 0, 0));

        backBtn.setPreferredSize(new Dimension((int) Math.round(310 * scaleX), (int) Math.round(60 * scaleY)));

        revalidate();
    }

    public void buildLevelButtons() {
        gridPanel.removeAll();

        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            final int levelId = i;
            boolean isUnlocked = (i <= unlockedLevel);

            JButton levelBtn = createLevelCardButton(levelId, isUnlocked);
            if (isUnlocked) {
                levelBtn.addActionListener(e -> handleLevelClick(levelId));
            }
            gridPanel.add(levelBtn);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton createLevelCardButton(int levelId, boolean isUnlocked) {
        JButton btn = new JButton() {
            private boolean isHovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isUnlocked) {
                            isHovered = true;
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int arc = Math.max(10, (int) (h * 0.16));

                if (isUnlocked) {
                    if (isHovered) {
                        g2.setColor(new Color(255, 255, 200, 55));
                        g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);
                    }

                    g2.setStroke(new BasicStroke(isHovered ? 5f : 3.5f));
                    g2.setColor(isHovered ? new Color(255, 240, 80) : new Color(245, 175, 25));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, arc, arc);
                } else {
                    g2.setColor(new Color(0, 0, 0, 145));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);

                    g2.setStroke(new BasicStroke(3f));
                    g2.setColor(new Color(90, 90, 90));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, arc, arc);

                    int fontSize = Math.max(11, (int) (h * 0.12));
                    g2.setColor(new Color(220, 220, 220));
                    g2.setFont(new Font("Arial", Font.BOLD, fontSize));
                    String text = "CHƯA MỞ";
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (w - fm.stringWidth(text)) / 2;
                    int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(text, tx, ty);
                }

                g2.dispose();
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(isUnlocked ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        btn.setEnabled(isUnlocked);

        return btn;
    }

    /**
     * Nút Quay lại Cài đặt hoàn toàn trong suốt, không viền, đè lên nút đỏ sẵn có trên ảnh
     */
    private JButton createTransparentBackButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(310, 60));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadBackgroundImage(String resourcePath, String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                backgroundImage = ImageIO.read(is);
                return;
            }
            File file = new File(filePath);
            if (file.exists()) {
                backgroundImage = ImageIO.read(file);
                return;
            }
            System.err.println("Không tìm thấy ảnh nền tại: " + resourcePath + " hoặc " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void handleLevelClick(int levelId) {
        Properties saveProps = SaveManager.loadGameProgress();

        if (saveProps != null) {
            int savedLevel = Integer.parseInt(saveProps.getProperty("currentLevel", "0"));

            if (savedLevel == levelId) {
                String[] options = {"Chơi tiếp (Dở trận)", "Chơi lại từ đầu", "Hủy"};
                int choice = JOptionPane.showOptionDialog(
                        this,
                        "Bạn đang có tiến trình chơi dở ở Cửa ải " + levelId + ". Bạn muốn tiếp tục hay chơi lại từ đầu?",
                        "Tùy chọn chơi",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == 0) {
                    parentFrame.startLevelWithSave(levelId, saveProps);
                    return;
                } else if (choice == 1) {
                    SaveManager.clearSaveFile();
                    parentFrame.startLevel(levelId);
                    return;
                } else {
                    return;
                }
            }
        }

        parentFrame.startLevel(levelId);
    }

    public static void setUnlockedLevel(int level) {
        unlockedLevel = level;
    }

    public static void unlockNextLevel(int completedLevel) {
        if (completedLevel >= unlockedLevel && completedLevel < TOTAL_LEVELS) {
            unlockedLevel = completedLevel + 1;
            SaveManager.saveGame(unlockedLevel);
        }
    }
}