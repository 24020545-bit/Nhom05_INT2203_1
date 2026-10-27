package UI;

import entity.Bot;
import entity.Player;
import entity.PlayerOb;

import java.awt.*;
import java.util.List;

public class HUDPanel {

    public static void draw(Graphics2D g2d, List<PlayerOb> players, int panelWidth, int panelHeight) {
        if (players == null || players.isEmpty()) return;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tọa độ cố định ở 4 góc màn hình cho tối đa 4 người chơi/bot (tăng chiều cao box lên 70px)
        int boxWidth = 120;
        int boxHeight = 70;

        int[][] positions = {
                {15, 15},                                           // Góc trên-trái (Player 1)
                {panelWidth - boxWidth - 15, 15},                   // Góc trên-phải (Bot 1)
                {15, panelHeight - boxHeight - 15},                 // Góc dưới-trái (Bot 2)
                {panelWidth - boxWidth - 15, panelHeight - boxHeight - 15} // Góc dưới-phải (Bot 3)
        };

        for (int i = 0; i < players.size() && i < positions.length; i++) {
            PlayerOb p = players.get(i);
            int boxX = positions[i][0];
            int boxY = positions[i][1];

            // Thiết kế khung nền đen mờ cho từng player (đủ chỗ chứa nhiều dòng)
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(boxX, boxY, boxWidth, boxHeight);

            // Viền màu phân biệt (Player thật màu Vàng, Bot màu Xanh dương nhạt)
            if (p instanceof Bot) {
                g2d.setColor(Color.CYAN);
            } else {
                g2d.setColor(Color.YELLOW);
            }
            g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

            // Chuẩn bị nội dung hiển thị
            String name = (p instanceof Player) ? "Player 1" : "Bot " + i;
            String status = "HP: " + p.getLives() + "\nBom: " +
                    (p.getBombLimit() - p.getActiveBombCount()) + "/" + p.getBombLimit() +
                    "\nSpeed: " + Math.round(p.getSpeed()*100)/100.0;

            g2d.setColor(Color.WHITE);

            // Vẽ tên người chơi ở dòng đầu tiên
            int currentY = boxY + 16;
            g2d.drawString(name, boxX + 8, currentY);

            // Tách chuỗi theo dấu \n và vẽ từng dòng thông tin xuống phía dưới
            String[] lines = status.split("\n");
            int lineHeight = 14; // Khoảng cách giữa các dòng

            for (String line : lines) {
                currentY += lineHeight;
                g2d.drawString(line, boxX + 8, currentY);
            }
        }
    }
}