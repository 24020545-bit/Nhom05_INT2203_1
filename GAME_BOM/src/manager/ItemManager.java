package manager;

import item.*;
import entity.Mod;

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

    // [MỚI THÊM] Xóa item tại tọa độ (x, y) khi bị lửa đốt trúng
    public void removeItem(int x, int y) {
        itemsList.removeIf(item -> item.getX() == x && item.getY() == y);
    }

    // 2. Kiểm tra va chạm: Nếu người chơi đi vào ô chứa item thì "ăn" item đó
    public void checkPlayerCollision(List<Mod> players) {
        if (players == null) return;

        for (Mod player : players) {
            if (!player.isAlive()) continue;

            int playerTileX = (int) Math.round(player.getX());
            int playerTileY = (int) Math.round(player.getY());

            for (Items item : itemsList) {
                if (!item.isCollected() && item.getX() == playerTileX && item.getY() == playerTileY) {
                    item.setCollected(true);
                    item.applyEffect(player);
                }
            }
        }

        itemsList.removeIf(Items::isCollected);
    }
}