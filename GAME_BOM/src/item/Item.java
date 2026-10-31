package item;

import entity.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.net.URL;
import javax.swing.ImageIcon;

public class Item {

    public enum ItemType {
        DAMAGE_UP(5),
        SPEED_UP(6),
        RANGE_UP(7),
        HEAL(8),
        BOMB_UP(9);

        private final int mapValue;

        ItemType(int mapValue) {
            this.mapValue = mapValue;
        }

        public int getMapValue() {
            return mapValue;
        }
    }

    private final double x;
    private final double y;
    private final int width = 32;
    private final int height = 32;
    private final ItemType type;
    private Image itemImage;
    private final int[][] mapHash;
    private boolean collected = false;

    public Item(double x, double y, ItemType type, int[][] mapHash) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.mapHash = mapHash;

        loadItemImage();
        updateMapHash(type.getMapValue());
    }

    private void loadItemImage() {
        String path = switch (type) {
            case DAMAGE_UP -> "/ItemImage/item_damage.png";
            case SPEED_UP  -> "/ItemImage/item_speed.png";
            case RANGE_UP  -> "/ItemImage/item_range.png";
            case HEAL      -> "/ItemImage/item_heal.png";
            case BOMB_UP   -> "/ItemImage/item_bomb.png";
        };

        try {
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                itemImage = new ImageIcon(imgUrl).getImage();
            } else {
                itemImage = null;
            }
        } catch (Exception e) {
            itemImage = null;
        }
    }

    public void update(Player player) {
        if (collected || player == null || !player.isSurvival()) return;

        // Kiểm tra va chạm với Player
        if (this.getBounds().intersects(player.getBounds())) {
            applyEffect(player);
            this.collected = true;
            updateMapHash(0); // Nhặt xong xóa khỏi mapHash (gán lại = 0 / EMPTY)
        }
    }

    private void applyEffect(Player player) {
        switch (type) {
            case DAMAGE_UP -> player.setAttackPower(player.getAttackPower() + 1);
            case SPEED_UP  -> {
                if (player.getSpeed() < 4) player.setSpeed(player.getSpeed() + 1);
            }
            case RANGE_UP  -> player.setPower(player.getPower() + 1);
            case HEAL      -> player.heal(1);
            case BOMB_UP   -> player.setBombLimit(player.getBombLimit() + 1);
        }
    }

    public void draw(Graphics2D g2d) {
        if (collected) return;

        if (itemImage != null) {
            g2d.drawImage(itemImage, (int) x, (int) y, width, height, null);
        } else {
            switch (type) {
                case DAMAGE_UP -> g2d.setColor(Color.RED);
                case SPEED_UP  -> g2d.setColor(Color.BLUE);
                case RANGE_UP  -> g2d.setColor(Color.ORANGE);
                case HEAL      -> g2d.setColor(Color.GREEN);
                case BOMB_UP   -> g2d.setColor(Color.MAGENTA);
            }
            g2d.fillRect((int) x, (int) y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public boolean isCollected() {
        return collected;
    }

    private void updateMapHash(int value) {
        final int tileSize = 32;
        try {
            int j = (int) (x / tileSize);
            int i = (int) (y / tileSize);
            if (mapHash != null && i >= 0 && i < mapHash.length && j >= 0 && j < mapHash[0].length) {
                if (value == 0 || mapHash[i][j] == 0 || mapHash[i][j] == type.getMapValue()) {
                    mapHash[i][j] = value;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật mapHash cho item: " + e.getMessage());
        }
    }
}