package bomb;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Bomb {
    private double x, y;
    private int[][] mapHash;
    private final int TILE_SIZE = 32;

    private int fuseTimer = 0;
    private int explodeTimer = 0;
    private boolean exploded = false;
    private boolean active = true;

    private int explosionRange = 2;
    private int damage = 1; // Sát thương nhận từ Player

    private int flameLeft = 0;
    private int flameRight = 0;
    private int flameUp = 0;
    private int flameDown = 0;

    private Image[] bombImages = new Image[3];
    private Image[] flameImages = new Image[3];
    private int animIndex = 0;
    private int animTimer = 0;

    // Constructor nhận trực tiếp range và damage từ Player truyền vào
    public Bomb(double x, double y, int[][] mapHash, int explosionRange, int damage) {
        this.x = ((int) (x / TILE_SIZE)) * TILE_SIZE;
        this.y = ((int) (y / TILE_SIZE)) * TILE_SIZE;
        this.mapHash = mapHash;
        this.explosionRange = explosionRange;
        this.damage = damage;

        loadImages();
        updateMapHash(5); // Ban đầu gán là 5 để cho phép người chơi đi qua an toàn
    }

    private void loadImages() {
        try {
            Image b1 = new ImageIcon(getClass().getResource("/BombImage/bom.png")).getImage();
            bombImages[0] = b1;
            bombImages[1] = b1;
            bombImages[2] = b1;

            for (int i = 0; i < 3; i++) {
                flameImages[i] = new ImageIcon(getClass().getResource("/BombImage/flame" + (i + 1) + ".png")).getImage();
            }
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh Bomb/Flame, sử dụng màu dự phòng!");
        }
    }

    public void update() {
        if (!active) return;

        animTimer++;
        if (animTimer >= 10) {
            animIndex = (animIndex + 1) % 3;
            animTimer = 0;
        }

        int j = (int) (x / TILE_SIZE);
        int i = (int) (y / TILE_SIZE);

        if (!exploded) {
            // Kiểm tra trực tiếp: Cứ có Player đang đứng đè lên ô bom này là giữ nguyên mã 5
            if (isPlayerStandingOnThisTile(j, i)) {
                mapHash[i][j] = 0; // Vẫn đang đứng trên bom -> Cho đi qua / đứng thoải mái không sợ kẹt
            } else {
                mapHash[i][j] = 4; // Đã bước ra ngoài hoàn toàn -> Khóa cứng thành vật cản ngay lập tức
            }

            fuseTimer++;
            if (fuseTimer >= 70) {
                explode();
            }
        } else {
            explodeTimer++;
            checkFlameDamage();

            if (explodeTimer >= 30) {
                updateMapHash(0);
                active = false;
            }
        }
    }

    // Hàm kiểm tra chính xác xem Player có đang đứng giao nhau với ô chứa bom này không
    private boolean isPlayerStandingOnThisTile(int bombTileX, int bombTileY) {
        for (Mob mob : Mobs.MOBS) {
            if (mob.isSurvival() && mob instanceof Player) {
                Rectangle playerBounds = mob.getBounds();
                Rectangle bombTileBounds = new Rectangle(bombTileX * TILE_SIZE, bombTileY * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (playerBounds.intersects(bombTileBounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void triggerExplosion() {
        if (!exploded && active) {
            explode();
        }
    }

    private void explode() {
        exploded = true;
        explodeTimer = 0;
        calculateFlameRange();
        updateMapHash(0);
    }

    private void calculateFlameRange() {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);

        flameRight = 0; flameLeft = 0; flameUp = 0; flameDown = 0;

        // Quét Phải
        for (int i = 1; i <= explosionRange; i++) {
            int targetX = tileX + i;
            if (targetX >= mapHash[0].length) break;
            int tileVal = mapHash[tileY][targetX];
            if (tileVal == 0 || tileVal == 5) {
                flameRight++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameRight++;
                    mapHash[tileY][targetX] = 0; // Phá tường
                }
                break;
            }
        }

        // Quét Trái
        for (int i = 1; i <= explosionRange; i++) {
            int targetX = tileX - i;
            if (targetX < 0) break;
            int tileVal = mapHash[tileY][targetX];
            if (tileVal == 0 || tileVal == 5) {
                flameLeft++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameLeft++;
                    mapHash[tileY][targetX] = 0;
                }
                break;
            }
        }

        // Quét Lên
        for (int i = 1; i <= explosionRange; i++) {
            int targetY = tileY - i;
            if (targetY < 0) break;
            int tileVal = mapHash[targetY][tileX];
            if (tileVal == 0 || tileVal == 5) {
                flameUp++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameUp++;
                    mapHash[targetY][tileX] = 0;
                }
                break;
            }
        }

        // Quét Xuống
        for (int i = 1; i <= explosionRange; i++) {
            int targetY = tileY + i;
            if (targetY >= mapHash.length) break;
            int tileVal = mapHash[targetY][tileX];
            if (tileVal == 0 || tileVal == 5) {
                flameDown++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameDown++;
                    mapHash[targetY][tileX] = 0;
                }
                break;
            }
        }
    }

    private void checkFlameDamage() {
        if (explodeTimer % 15 != 0) return;

        Rectangle bombCenterRect = getBounds();

        for (Mob mob : Mobs.MOBS) {
            if (!mob.isSurvival()) continue;

            boolean hit = false;
            Rectangle mobBounds = mob.getBounds();

            if (mobBounds.intersects(bombCenterRect)) hit = true;

            for (int i = 1; i <= flameRight; i++) {
                if (mobBounds.intersects(new Rectangle((int) x + i * TILE_SIZE, (int) y, TILE_SIZE, TILE_SIZE))) hit = true;
            }
            for (int i = 1; i <= flameLeft; i++) {
                if (mobBounds.intersects(new Rectangle((int) x - i * TILE_SIZE, (int) y, TILE_SIZE, TILE_SIZE))) hit = true;
            }
            for (int i = 1; i <= flameUp; i++) {
                if (mobBounds.intersects(new Rectangle((int) x, (int) y - i * TILE_SIZE, TILE_SIZE, TILE_SIZE))) hit = true;
            }
            for (int i = 1; i <= flameDown; i++) {
                if (mobBounds.intersects(new Rectangle((int) x, (int) y + i * TILE_SIZE, TILE_SIZE, TILE_SIZE))) hit = true;
            }

            if (hit) {
                mob.takeDamage(this.damage);
            }
        }
    }

    public void draw(Graphics2D g2d) {
        if (!active) return;

        int ix = (int) x;
        int iy = (int) y;

        if (!exploded) {
            if (bombImages[animIndex] != null) {
                g2d.drawImage(bombImages[animIndex], ix, iy, TILE_SIZE, TILE_SIZE, null);
            } else if (bombImages[0] != null) {
                g2d.drawImage(bombImages[0], ix, iy, TILE_SIZE, TILE_SIZE, null);
            } else {
                g2d.setColor(java.awt.Color.DARK_GRAY);
                g2d.fillOval(ix + 4, iy + 4, 24, 24);
            }
        } else {
            if (flameImages[0] != null) {
                Image bodyImg = (flameImages[1] != null) ? flameImages[1] : flameImages[0];
                Image tipImg = (flameImages[2] != null) ? flameImages[2] : bodyImg;

                // 1. Vẽ Tâm nổ
                g2d.drawImage(flameImages[0], ix, iy, TILE_SIZE, TILE_SIZE, null);

                // 2. Vẽ Tia Phải
                for (int i = 1; i <= flameRight; i++) {
                    if (i == flameRight) {
                        g2d.drawImage(tipImg, ix + i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2d.drawImage(bodyImg, ix + i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    }
                }

                // 3. Vẽ Tia Trái
                for (int i = 1; i <= flameLeft; i++) {
                    if (i == flameLeft) {
                        g2d.drawImage(tipImg, ix - i * TILE_SIZE + TILE_SIZE, iy, -TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2d.drawImage(bodyImg, ix - i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    }
                }

                // 4. Vẽ Tia Xuống
                for (int i = 1; i <= flameDown; i++) {
                    Graphics2D g2dRot = (Graphics2D) g2d.create();
                    int cx = ix + TILE_SIZE / 2;
                    int cy = (iy + i * TILE_SIZE) + TILE_SIZE / 2;
                    g2dRot.rotate(Math.toRadians(90), cx, cy);

                    if (i == flameDown) {
                        g2dRot.drawImage(tipImg, ix, iy + i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2dRot.drawImage(bodyImg, ix, iy + i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    }
                    g2dRot.dispose();
                }

                // 5. Vẽ Tia Lên
                for (int i = 1; i <= flameUp; i++) {
                    Graphics2D g2dRot = (Graphics2D) g2d.create();
                    int cx = ix + TILE_SIZE / 2;
                    int cy = (iy - i * TILE_SIZE) + TILE_SIZE / 2;
                    g2dRot.rotate(Math.toRadians(-90), cx, cy);

                    if (i == flameUp) {
                        g2dRot.drawImage(tipImg, ix, iy - i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2dRot.drawImage(bodyImg, ix, iy - i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    }
                    g2dRot.dispose();
                }
            } else {
                g2d.setColor(java.awt.Color.ORANGE);
                g2d.fillRect(ix, iy, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, TILE_SIZE, TILE_SIZE);
    }

    private void updateMapHash(int value) {
        try {
            int j = (int) (x / TILE_SIZE);
            int i = (int) (y / TILE_SIZE);
            if (mapHash != null && i >= 0 && i < mapHash.length && j >= 0 && j < mapHash[0].length) {
                mapHash[i][j] = value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isExploded() {
        return exploded;
    }
}