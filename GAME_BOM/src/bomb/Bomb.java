package bomb;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;
import utils.SoundManager; // THÊM IMPORT

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;

public class Bomb {
    private final double x;
    private final double y;
    private final int[][] mapHash;
    private final List<Bomb> globalBombs;
    private final int TILE_SIZE = 32;

    private int fuseTimer = 0;
    private int explodeTimer = 0;
    private boolean exploded = false;
    private boolean active = true;

    private final int explosionRange;
    private final int damage;

    private int flameLeft = 0;
    private int flameRight = 0;
    private int flameUp = 0;
    private int flameDown = 0;

    private final Image[] bombImages = new Image[5];
    private int animIndex = 0;
    private int animTimer = 0;

    private Image flameMid;
    private Image flameHorizontal;
    private Image flameVertical;
    private Image flameTop;
    private Image flameBot;
    private Image flameLeftImg;
    private Image flameRightImg;

    /** Khởi tạo quả bom, căn chỉnh tọa độ và tải tài nguyên hình ảnh */
    public Bomb(double x, double y, int[][] mapHash, List<Bomb> globalBombs, int explosionRange, int damage) {
        this.x = ((int) (x / TILE_SIZE)) * TILE_SIZE;
        this.y = ((int) (y / TILE_SIZE)) * TILE_SIZE;
        this.mapHash = mapHash;
        this.globalBombs = globalBombs;
        this.explosionRange = explosionRange;
        this.damage = damage;

        loadImages();
        updateMapHash(5);
    }

    /** Tải toàn bộ tài nguyên hình ảnh bom và hiệu ứng lửa */
    private void loadImages() {
        try {
            for (int i = 0; i < 5; i++) {
                URL imgUrl = getClass().getResource("/BombImage/bom" + (i + 1) + ".png");
                if (imgUrl != null) {
                    bombImages[i] = new ImageIcon(imgUrl).getImage();
                }
            }

            flameMid = loadImageSafely("/BombImage/flame_mid.png");
            flameHorizontal = loadImageSafely("/BombImage/flame_h.png");
            flameVertical = loadImageSafely("/BombImage/flame_v.png");
            flameTop = loadImageSafely("/BombImage/flame_top.png");
            flameBot = loadImageSafely("/BombImage/flame_bot.png");
            flameLeftImg = loadImageSafely("/BombImage/flame_left.png");
            flameRightImg = loadImageSafely("/BombImage/flame_right.png");

        } catch (Exception e) {
            System.err.println("Lỗi tải tài nguyên hình ảnh Bomb/Flame: " + e.getMessage());
        }
    }

    /** Tải tệp ảnh an toàn, trả về null nếu không tìm thấy */
    private Image loadImageSafely(String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        System.err.println("Không tìm thấy tệp ảnh: " + path);
        return null;
    }

    /** Cập nhật trạng thái của bom theo từng khung hình (tick) */
    public void update() {
        if (!active) return;

        animTimer++;
        if (animTimer >= 6) {
            animIndex = (animIndex + 1) % 5;
            animTimer = 0;
        }

        int j = (int) (x / TILE_SIZE);
        int i = (int) (y / TILE_SIZE);

        if (!exploded) {
            if (isPlayerStandingOnThisTile(j, i)) {
                mapHash[i][j] = 0;
            } else {
                mapHash[i][j] = 4;
            }

            fuseTimer++;
            if (fuseTimer >= 70) {
                explode();
            }
        } else {
            explodeTimer++;
            checkFlameDamage();

            if (explodeTimer >= 20) {
                updateMapHash(0);
                active = false;
            }
        }
    }

    /** Kiểm tra xem người chơi có đang đứng đè lên ô chứa bom hay không */
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

    /** Kích nổ quả bom ngay lập tức từ bên ngoài */
    public void triggerExplosion() {
        if (!exploded && active) {
            explode();
        }
    }

    /** Chuyển đổi trạng thái bom sang phát nổ */
    private void explode() {
        if (exploded) return;
        exploded = true;
        explodeTimer = 0;
        calculateFlameRange();
        updateMapHash(0);

        // THÊM: Gọi hàm phát âm thanh bom nổ
        SoundManager.playSoundEffect("/audio/boom.wav");
    }

    /** Kiểm tra và kích nổ lan các quả bom khác nằm trong tầm lửa */
    private void checkAndTriggerOtherBomb(int targetTileX, int targetTileY) {
        if (globalBombs == null) return;
        for (Bomb b : globalBombs) {
            if (b != this && b.isActive() && !b.isExploded()) {
                int bx = (int) (b.x / TILE_SIZE);
                int by = (int) (b.y / TILE_SIZE);
                if (bx == targetTileX && by == targetTileY) {
                    b.triggerExplosion();
                }
            }
        }
    }

    /** Tính toán phạm vi lan rộng của tia lửa theo 4 hướng */
    private void calculateFlameRange() {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);

        flameRight = 0; flameLeft = 0; flameUp = 0; flameDown = 0;

        for (int i = 1; i <= explosionRange; i++) {
            int targetX = tileX + i;
            if (targetX >= mapHash[0].length) break;

            checkAndTriggerOtherBomb(targetX, tileY);

            int tileVal = mapHash[tileY][targetX];
            if (tileVal == 0 || tileVal == 4 || tileVal == 5) {
                flameRight++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameRight++;
                    mapHash[tileY][targetX] = 0;
                }
                break;
            }
        }

        for (int i = 1; i <= explosionRange; i++) {
            int targetX = tileX - i;
            if (targetX < 0) break;

            checkAndTriggerOtherBomb(targetX, tileY);

            int tileVal = mapHash[tileY][targetX];
            if (tileVal == 0 || tileVal == 4 || tileVal == 5) {
                flameLeft++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameLeft++;
                    mapHash[tileY][targetX] = 0;
                }
                break;
            }
        }

        for (int i = 1; i <= explosionRange; i++) {
            int targetY = tileY - i;
            if (targetY < 0) break;

            checkAndTriggerOtherBomb(tileX, targetY);

            int tileVal = mapHash[targetY][tileX];
            if (tileVal == 0 || tileVal == 4 || tileVal == 5) {
                flameUp++;
            } else {
                if (tileVal == 1 || tileVal == 3) {
                    flameUp++;
                    mapHash[targetY][tileX] = 0;
                }
                break;
            }
        }

        for (int i = 1; i <= explosionRange; i++) {
            int targetY = tileY + i;
            if (targetY >= mapHash.length) break;

            checkAndTriggerOtherBomb(tileX, targetY);

            int tileVal = mapHash[targetY][tileX];
            if (tileVal == 0 || tileVal == 4 || tileVal == 5) {
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

    /** Kiểm tra va chạm của lửa để gây sát thương lên các thực thể */
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

    /** Vẽ hình ảnh quả bom hoặc hiệu ứng lửa lên màn hình */
    public void draw(Graphics2D g2d) {
        if (!active) return;

        int ix = (int) x;
        int iy = (int) y;

        if (!exploded) {
            if (bombImages[animIndex] != null) {
                g2d.drawImage(bombImages[animIndex], ix, iy, TILE_SIZE, TILE_SIZE, null);
            } else {
                g2d.setColor(java.awt.Color.DARK_GRAY);
                g2d.fillOval(ix + 4, iy + 4, 24, 24);
            }
        } else {
            if (flameMid != null) {
                g2d.drawImage(flameMid, ix, iy, TILE_SIZE, TILE_SIZE, null);

                for (int i = 1; i <= flameRight; i++) {
                    if (i == flameRight) {
                        g2d.drawImage(flameRightImg, ix + i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2d.drawImage(flameHorizontal, ix + i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    }
                }

                for (int i = 1; i <= flameLeft; i++) {
                    if (i == flameLeft) {
                        g2d.drawImage(flameLeftImg, ix - i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2d.drawImage(flameHorizontal, ix - i * TILE_SIZE, iy, TILE_SIZE, TILE_SIZE, null);
                    }
                }

                for (int i = 1; i <= flameDown; i++) {
                    if (i == flameDown) {
                        g2d.drawImage(flameBot, ix, iy + i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2d.drawImage(flameVertical, ix, iy + i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    }
                }

                for (int i = 1; i <= flameUp; i++) {
                    if (i == flameUp) {
                        g2d.drawImage(flameTop, ix, iy - i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g2d.drawImage(flameVertical, ix, iy - i * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    }
                }
            } else {
                g2d.setColor(java.awt.Color.ORANGE);
                g2d.fillRect(ix, iy, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    /** Trả về khung chữ nhật giới hạn va chạm của quả bom */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, TILE_SIZE, TILE_SIZE);
    }

    /** Cập nhật giá trị trạng thái ô lưới trên mảng bản đồ */
    private void updateMapHash(int value) {
        try {
            int j = (int) (x / TILE_SIZE);
            int i = (int) (y / TILE_SIZE);
            if (mapHash != null && i >= 0 && i < mapHash.length && j >= 0 && j < mapHash[0].length) {
                mapHash[i][j] = value;
            }
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật mapHash: " + e.getMessage());
        }
    }

    /** Trả về trạng thái hoạt động của bom */
    public boolean isActive() {
        return active;
    }

    /** Trả về trạng thái quả bom đã phát nổ hay chưa */
    public boolean isExploded() {
        return exploded;
    }
}