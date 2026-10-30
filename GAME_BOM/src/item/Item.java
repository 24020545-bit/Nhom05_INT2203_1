package item;


import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;
import utils.Direction;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Item extends Mob {

    // Định nghĩa 4 loại vật phẩm theo yêu cầu
    public enum ItemType {
        DAMAGE_UP(6),  // Gán mã số 6 trên mapHash cho item Tăng sát thương
        SPEED_UP(7),   // Gán mã số 7 trên mapHash cho item Tăng tốc độ
        RANGE_UP(8),   // Gán mã số 8 trên mapHash cho item Tăng phạm vi bom
        HEAL(9);       // Gán mã số 9 trên mapHash cho item Hồi máu

        private final int mapValue;

        ItemType(int mapValue) {
            this.mapValue = mapValue;
        }

        public int getMapValue() {
            return mapValue;
        }
    }

    private ItemType type;
    private Image itemImage;
    private final int TILE_SIZE = 32;
    private int[][] mapHash;

    public Item(double x, double y, ItemType type, int[][] mapHash) {
        super(x, y, 32, 32, 0, 0, 0, 0, 0);
        this.type = type;
        this.mapHash = mapHash;
        this.survival = true;

        loadItemImage();
        // Đánh dấu lên mapHash bằng đúng giá trị riêng của từng loại item
        updateMapHash(type.getMapValue());
    }

    private void loadItemImage() {
        try {
            switch (type) {
                case DAMAGE_UP:
                    itemImage = new ImageIcon(getClass().getResource("/ItemImage/item_damage.png")).getImage();
                    break;
                case SPEED_UP:
                    itemImage = new ImageIcon(getClass().getResource("/ItemImage/item_speed.png")).getImage();
                    break;
                case RANGE_UP:
                    itemImage = new ImageIcon(getClass().getResource("/ItemImage/item_range.png")).getImage();
                    break;
                case HEAL:
                    itemImage = new ImageIcon(getClass().getResource("/ItemImage/item_heal.png")).getImage();
                    break;
            }
        } catch (Exception e) {
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
                    updateMapHash(0); // Khi nhặt xong, xóa khỏi mapHash (trả về 0)
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
        try {
            int j = (int) (x / TILE_SIZE);
            int i = (int) (y / TILE_SIZE);
            if (mapHash != null && i >= 0 && i < mapHash.length && j >= 0 && j < mapHash[0].length) {
                // Nếu giá trị cần set là 0 (xóa), hoặc ô đó đang đúng là mã của item này thì mới cập nhật
                if (value == 0 || mapHash[i][j] == 0 || mapHash[i][j] == type.getMapValue()) {
                    mapHash[i][j] = value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}