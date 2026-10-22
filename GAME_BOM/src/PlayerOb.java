public abstract class PlayerOb {
    // Các thuộc tính riêng tư/được bảo vệ theo biểu đồ UML
    protected int x;
    protected int y;
    protected double speed;
    protected int lives;
    protected int bombLimit;
    protected int activeBombCount;
    protected boolean isAlive;

    // Thuộc tính mở rộng để quả bom biết bán kính nổ (power) của người đặt
    protected int power;

    /**
     * Constructor khởi tạo thông tin cho nhân vật
     */
    public PlayerOb(int x, int y, double speed, int lives, int bombLimit, int power) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.lives = lives;
        this.bombLimit = bombLimit;
        this.power = power;
        this.activeBombCount = 0;
        this.isAlive = true;
    }

    /**
     * Phương thức di chuyển theo hướng truyền vào (Abstract để Player và Bot tự định nghĩa)
     */
    public abstract void move(Direction direction);

    /**
     * Phương thức đặt bom[cite: 1]
     * @param gameMap Bản đồ hiện tại để truyền vào quả bom
     */
    public void placeBomb(GameMap gameMap) {
        // Nếu nhân vật đã chết thì không thể đặt bom
        if (!isAlive) {
            return;
        }

        // Kiểm tra xem số lượng bom đang đặt có vượt quá giới hạn (bombLimit) không
        if (activeBombCount < bombLimit) {
            // Tạo quả bom mới tại tọa độ hiện tại của nhân vật
            Bomb newBomb = new Bomb(this.x, this.y, this.power, this, gameMap);

            // Thêm bom vào danh sách quản lý của bản đồ
            if (gameMap != null) {
                gameMap.addBomb(newBomb);
            }

            // Tăng số lượng bom đang hoạt động của nhân vật lên
            activeBombCount++;
        }
    }

    /**
     * Phương thức nhận sát thương[cite: 1] khi chạm lửa hoặc kẻ địch
     */
    public void takeDamage() {
        if (!isAlive) {
            return;
        }

        lives--;
        if (lives <= 0) {
            die();
        }
    }

    /**
     * Phương thức xử lý khi nhân vật chết[cite: 1]
     */
    public void die() {
        this.isAlive = false;
        // Các logic phụ trợ khi chết (ví dụ: phát âm thanh, dừng di chuyển, v.v.)
    }

    // ==========================================
    // GETTERS & SETTERS (Phục vụ quản lý trạng thái)
    // ==========================================

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public int getBombLimit() { return bombLimit; }
    public void setBombLimit(int bombLimit) { this.bombLimit = bombLimit; }

    public int getActiveBombCount() { return activeBombCount; }
    public void setActiveBombCount(int activeBombCount) { this.activeBombCount = activeBombCount; }

    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }

    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
}