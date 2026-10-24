package UI;

import Player.Bot;
import Player.Player;
import Player.PlayerOb;

import java.awt.*;
import java.util.List;

public class HUDPanel {

    public static void draw(Graphics2D g2d, List<PlayerOb> players, int panelWidth, int panelHeight) {
        if (players == null || players.isEmpty()) return;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tọa độ cố định ở 4 góc màn hình cho tối đa 4 người chơi/bot
        int[][] positions = {
                {15, 15},                               // Góc trên-trái (Player 1)
                {panelWidth - 135, 15},                 // Góc trên-phải (Bot 1)
                {15, panelHeight - 45},                 // Góc dưới-trái (Bot 2)
                {panelWidth - 135, panelHeight - 45}    // Góc dưới-phải (Bot 3)
        };

        for (int i = 0; i < players.size() && i < positions.length; i++) {
            PlayerOb p = players.get(i);
            int boxX = positions[i][0];
            int boxY = positions[i][1];

            // Thiết kế khung nền đen mờ cho từng player
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(boxX, boxY, 120, 30);

            // Viền màu phân biệt (Player thật màu Vàng, Bot màu Xanh dương nhạt)
            if (p instanceof Bot) {
                g2d.setColor(Color.CYAN);
            } else {
                g2d.setColor(Color.YELLOW);
            }
            g2d.drawRect(boxX, boxY, 120, 30);

            // Sửa lại tên hiển thị chuẩn xác (tránh lỗi cộng chuỗi số)
            String name = (p instanceof Player) ? "Player 1" : "Bot " + i;
            String status = "HP: " + p.getLives() + " | Bom: " + (p.getBombLimit() - p.getActiveBombCount());

            g2d.setColor(Color.WHITE);
            g2d.drawString(name, boxX + 8, boxY + 12);
            g2d.drawString(status, boxX + 8, boxY + 24);
        }
    }
}