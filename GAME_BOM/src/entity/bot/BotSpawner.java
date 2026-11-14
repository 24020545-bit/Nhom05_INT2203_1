
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
        double baseSpeed=0;
        double baseHp=0;
        double basePower=0;
        double baseCooldown=0;

        switch (type) {
            case "Doll":
                baseSpeed = Math.min(5, 2+(levelIndex-1)*0.25);
                baseHp = 5+(levelIndex-1)*2;
                basePower = 1*(1+(levelIndex-1)*0.25);
                baseCooldown = Math.max(500, 1000*(1-(levelIndex-1)*0.05));
                break;

            case "Kondoria":
                baseSpeed = Math.min(4, 1+(levelIndex-1)*0.25);
                baseHp = 4+(levelIndex-1)*2;
                basePower = 1+(levelIndex-1)*0.25;
                baseCooldown = Math.max(750, 1500*(1-(levelIndex-1)*0.05));
                break;

            case "Boss":
                baseSpeed = Math.min(4, 1+(levelIndex-1)*0.25);
                baseHp = 20+(levelIndex-1)*3;
                baseCooldown = Math.max(4000, 8000*(1-(levelIndex-1)*0.05));
                break;

            default:
                return;
        }

        // Chi so cuoi cung: su dung so nguyen
        int finalHp = (int)baseHp + playerBonus;
        int finalSpeed = (int)baseSpeed;
        int finalPower = (int)basePower;
        long finalCooldown = (int)baseCooldown;

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
