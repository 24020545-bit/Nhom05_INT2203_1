package item;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;
import utils.Direction;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

public class Item extends Mob {

    // Định nghĩa các loại vật phẩm
    public enum ItemType {
        DAMAGE_UP(6),  // Tăng sát thương (mã 6)
        SPEED_UP(7),   // Tăng tốc độ (mã 7)
        RANGE_UP(8),   // Tăng phạm vi bom (mã 8)
        HEAL(9),       // Hồi máu (mã 9)
        BOMB_UP(10);   // Tăng số lượng bom tối đa (mã 10)

        private final int mapValue;

        ItemType(int mapValue) {
            this.mapValue = mapValue;
        }

        public int getMapValue() {
            return mapValue;
        }
    }

    private final ItemType type;
    private Image itemImage;
    private final int[][] mapHash;

    public Item(double x, double y, ItemType type, int[][] mapHash) {
        super(x, y, 32, 32, 0, 0, 0, 0, 0);
        this.type = type;
        this.mapHash = mapHash;
        this.survival = true;

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
                System.err.println("Không tìm thấy tệp ảnh item: " + path);
                itemImage = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh item " + path + ": " + e.getMessage());
            itemImage = null;
        }
    }

    @Override
    public void update() {
        if (!survival) return;

        for (Mob mob : Mobs.MOBS) {
            if (mob instanceof Player && mob.isSurvival()) {
                if (this.getBounds().intersects(mob.getBounds())) {
                    applyEffect((Player) mob);
                    this.survival = false;
                    updateMapHash(0); // Nhặt xong xóa khỏi mapHash
                }
            }
        }
    }

    private void applyEffect(Player player) {
        switch (type) {
            case DAMAGE_UP:
                player.setAttackPower(player.getAttackPower() + 1);
                break;
            case SPEED_UP:
                if (player.getSpeed() < 4) {
                    player.setSpeed(player.getSpeed() + 1);
                }
                break;
            case RANGE_UP:
                player.setPower(player.getPower() + 1);
                break;
            case HEAL:
                player.heal(1);
                break;
            case BOMB_UP:
                player.setBombLimit(player.getBombLimit() + 1);
                break;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (!survival) return;

        if (itemImage != null) {
            g2d.drawImage(itemImage, (int) x, (int) y, width, height, null);
        } else {
            switch (type) {
                case DAMAGE_UP: g2d.setColor(Color.RED); break;
                case SPEED_UP:  g2d.setColor(Color.BLUE); break;
                case RANGE_UP:  g2d.setColor(Color.ORANGE); break;
                case HEAL:      g2d.setColor(Color.GREEN); break;
                case BOMB_UP:   g2d.setColor(Color.MAGENTA); break;
            }
            g2d.fillRect((int) x, (int) y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int) x, (int) y, width, height);
        }
    }

    @Override
    public void move(Direction direction) {
        // Đứng yên
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