package UI.Screen;

import manager.KeyConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeySettingsPanel extends JPanel {
    private JButton btnP1Up, btnP1Down, btnP1Left, btnP1Right, btnP1Bomb;
    private JButton btnP2Up, btnP2Down, btnP2Left, btnP2Right, btnP2Bomb;
    private JButton btnBack;

    private JButton waitingButton = null; // Nút đang chờ gán phím mới

    public KeySettingsPanel(Runnable onBackClicked) {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 40));

        // Tiêu đề
        JLabel titleLabel = new JLabel("CÀI ĐẶT PHÍM ĐIỀU KHIỂN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panel chứa cài đặt phím cho 2 Player (chia 2 cột)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        centerPanel.add(createPlayerConfigPanel("PLAYER 1",
                btnP1Up = createKeyButton(KeyEvent.getKeyText(KeyConfig.p1Up)),
                btnP1Down = createKeyButton(KeyEvent.getKeyText(KeyConfig.p1Down)),
                btnP1Left = createKeyButton(KeyEvent.getKeyText(KeyConfig.p1Left)),
                btnP1Right = createKeyButton(KeyEvent.getKeyText(KeyConfig.p1Right)),
                btnP1Bomb = createKeyButton(KeyEvent.getKeyText(KeyConfig.p1Bomb))
        ));

        centerPanel.add(createPlayerConfigPanel("PLAYER 2",
                btnP2Up = createKeyButton(KeyEvent.getKeyText(KeyConfig.p2Up)),
                btnP2Down = createKeyButton(KeyEvent.getKeyText(KeyConfig.p2Down)),
                btnP2Left = createKeyButton(KeyEvent.getKeyText(KeyConfig.p2Left)),
                btnP2Right = createKeyButton(KeyEvent.getKeyText(KeyConfig.p2Right)),
                btnP2Bomb = createKeyButton(KeyEvent.getKeyText(KeyConfig.p2Bomb))
        ));

        add(centerPanel, BorderLayout.CENTER);

        // Nút quay lại
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        btnBack = new JButton("QUAY LẠI");
        btnBack.setFont(new Font("Arial", Font.BOLD, 16));
        btnBack.setPreferredSize(new Dimension(150, 40));
        btnBack.addActionListener(e -> onBackClicked.run());
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        // Lắng nghe sự kiện bấm phím để thay đổi
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (waitingButton != null) {
                    int newKeyCode = e.getKeyCode();
                    String newKeyText = KeyEvent.getKeyText(newKeyCode);

                    waitingButton.setText(newKeyText);
                    assignKeyToConfig(waitingButton, newKeyCode);

                    waitingButton.setBackground(Color.LIGHT_GRAY);
                    waitingButton = null;
                }
            }
        });
    }

    private JPanel createPlayerConfigPanel(String playerName, JButton bUp, JButton bDown, JButton bLeft, JButton bRight, JButton bBomb) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(45, 45, 60));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), playerName,
                javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18), Color.WHITE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        addConfigRow(panel, gbc, "Lên (UP):", bUp);
        addConfigRow(panel, gbc, "Xuống (DOWN):", bDown);
        addConfigRow(panel, gbc, "Trái (LEFT):", bLeft);
        addConfigRow(panel, gbc, "Phải (RIGHT):", bRight);
        addConfigRow(panel, gbc, "Đặt Bom (BOMB):", bBomb);

        return panel;
    }

    private void addConfigRow(JPanel panel, GridBagConstraints gbc, String labelText, JButton btn) {
        gbc.gridx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(btn, gbc);
        gbc.gridy++;
    }

    private JButton createKeyButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 30));
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(Color.LIGHT_GRAY);

        btn.addActionListener(e -> {
            if (waitingButton != null) {
                waitingButton.setBackground(Color.LIGHT_GRAY);
            }
            waitingButton = btn;
            waitingButton.setBackground(Color.ORANGE);
            waitingButton.setText("Nhấn phím...");
            requestFocusInWindow();
        });

        return btn;
    }

    private void assignKeyToConfig(JButton btn, int keyCode) {
        if (btn == btnP1Up) KeyConfig.p1Up = keyCode;
        else if (btn == btnP1Down) KeyConfig.p1Down = keyCode;
        else if (btn == btnP1Left) KeyConfig.p1Left = keyCode;
        else if (btn == btnP1Right) KeyConfig.p1Right = keyCode;
        else if (btn == btnP1Bomb) KeyConfig.p1Bomb = keyCode;

        else if (btn == btnP2Up) KeyConfig.p2Up = keyCode;
        else if (btn == btnP2Down) KeyConfig.p2Down = keyCode;
        else if (btn == btnP2Left) KeyConfig.p2Left = keyCode;
        else if (btn == btnP2Right) KeyConfig.p2Right = keyCode;
        else if (btn == btnP2Bomb) KeyConfig.p2Bomb = keyCode;
    }
}