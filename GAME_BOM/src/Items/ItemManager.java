package Items;

import Player.PlayerOb;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemManager {
    private final List<Items> itemsList = new CopyOnWriteArrayList<>();
    private final Random random = new Random();

    public List<Items> getItemsList() {
        return itemsList;
    }

    // 1. Hàm sinh ngẫu nhiên item tại tọa độ (x, y) khi tường bị phá
    public void spawnItem(int x, int y) {
        // Tỷ lệ xuất hiện item (ví dụ: 40% có item, 60% không có gì)
        if (random.nextInt(100) < 40) {
            int type = random.nextInt(4); // 4 loại item khác nhau
            Items newItem = null;

            switch (type) {
                case 0:
                    newItem = new SpeedUp(x, y);
                    break;
                case 1:
                    newItem = new BombLimitUp(x, y);
                    break;
                case 2:
                    newItem = new LiveUp(x, y);
                    break;
                case 3:
                    newItem = new PowerUp(x, y);
                    break;
            }

            if (newItem != null) {
                itemsList.add(newItem);
            }
        }
    }

    // 2. Kiểm tra va chạm: Nếu người chơi đi vào ô chứa item thì "ăn" item đó
    public void checkPlayerCollision(List<PlayerOb> players) {
        if (players == null) return;

        for (PlayerOb player : players) {
            if (!player.isAlive()) continue;

            // Lấy tọa độ ô (Tile) hiện tại của nhân vật
            int playerTileX = (int) player.getX();
            int playerTileY = (int) player.getY();

            for (Items item : itemsList) {
                if (!item.isCollected() && item.getX() == playerTileX && item.getY() == playerTileY) {
                    item.setCollected(true); // Đánh dấu item đã được nhặt
                    item.applyEffect(player); // Kích hoạt hiệu ứng cho nhân vật
                }
            }
        }

        // Dọn dẹp các item đã bị ăn ra khỏi danh sách để tối ưu bộ nhớ
        itemsList.removeIf(Items::isCollected);
    }
}