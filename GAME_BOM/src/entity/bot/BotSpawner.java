
package entity.bot;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;

public class BotSpawner {

    private static final int TILE_SIZE = 32;
    private static int[][] currentGrid;

    public static void setGrid(int[][] grid) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid cannot be null!");
        }
        currentGrid = grid;
    }

    public static void spawn(String type, int tileX, int tileY,
                             int levelIndex, int numPlayers) {

        // Kiem tra du lieu dau vao
        if (currentGrid == null) {
            throw new IllegalStateException("Grid has not been initialized!");
        }

        if (levelIndex < 1 || numPlayers < 1) {
            throw new IllegalArgumentException("Invalid level or player count!");
        }

        if (tileY < 0 || tileY >= currentGrid.length
                || tileX < 0 || tileX >= currentGrid[tileY].length) {
            throw new IllegalArgumentException("Invalid spawn position!");
        }

        // Lay danh sach nguoi choi con song
        Player[] allPlayers = Mobs.MOBS.stream()
                .filter(m -> m instanceof Player && m.isSurvival())
                .map(m -> (Player) m)
                .toArray(Player[]::new);

        Player target = allPlayers.length > 0 ? allPlayers[0] : null;

        // Bonus HP khi choi 2 nguoi
        int playerBonus = numPlayers >= 2 ? 3 : 0;

        // Chi so co ban
        int baseSpeed = 1;
        int baseHp = 3;
        int basePower = 1;
        long baseCooldown = 1500;

        switch (type) {
            case "Doll":
                baseSpeed = 2;
                baseHp = (1 + (levelIndex - 1) * 2) / 2;
                basePower = 1 + (levelIndex - 1) / 3;
                baseCooldown = Math.max(400, 1200 - (levelIndex - 1) * 100);
                break;

            case "Kondoria":
                baseSpeed = 1 + (levelIndex - 1) / 4;
                baseHp = (4 + (levelIndex - 1) * 2) / 2;
                basePower = 1 + (levelIndex - 1) / 3;
                baseCooldown = Math.max(300, 1000 - (levelIndex - 1) * 80);
                break;

            case "Boss":
                baseSpeed = 1 + (levelIndex - 1) / 4;
                baseHp = (25 + (levelIndex - 1) * 10) / 2;
                baseCooldown = Math.max(3000, 6000 - (levelIndex - 1) * 500);
                break;

            default:
                return;
        }

        // Chi so cuoi cung: su dung so nguyen
        int finalHp = baseHp + playerBonus;
        int finalSpeed = baseSpeed;
        int finalPower = basePower;
        long finalCooldown = baseCooldown;

        double x = tileX * TILE_SIZE;
        double y = tileY * TILE_SIZE;

        // Khoi tao bot
        Mob bot = switch (type) {
            case "Doll" -> new DollBot(
                    x, y, TILE_SIZE, TILE_SIZE,
                    finalSpeed, finalHp, finalPower,
                    finalCooldown, target, currentGrid
            );

            case "Kondoria" -> new KondoriaBot(
                    x, y, TILE_SIZE, TILE_SIZE,
                    finalSpeed, finalHp, finalPower,
                    finalCooldown, allPlayers, currentGrid
            );

            case "Boss" -> new BossBot(
                    x, y, TILE_SIZE, TILE_SIZE,
                    finalSpeed, finalHp, finalCooldown, currentGrid
            );

            default -> null;
        };

        if (bot != null) {
            Mobs.add(bot);
        }
    }
}
