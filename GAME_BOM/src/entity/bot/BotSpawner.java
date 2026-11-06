package entity.bot;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;

public class BotSpawner {

    private static final int TILE_SIZE = 32;
    private static int[][] currentGrid;

    public static void setGrid(int[][] grid) {
        currentGrid = grid;
    }

    public static void spawn(String type, int tileX, int tileY, int levelIndex, int numPlayers) {

        Player[] allPlayers = Mobs.MOBS.stream()
                .filter(m -> m instanceof Player && m.isSurvival())
                .map(m -> (Player) m)
                .toArray(Player[]::new);

        Player target = (allPlayers.length > 0) ? allPlayers[0] : null;

        double playerMultiplier = (numPlayers >= 2) ? 1.25 : 1.0;
        double levelMultiplier = 1.0 + (levelIndex - 1) * 0.2;
        double totalMultiplier = playerMultiplier * levelMultiplier;

        int baseSpeed = 1;
        int baseHp = 3;
        int basePower = 1;
        long baseCooldown = 1500;

        switch (type) {
            case "Doll":
                baseSpeed = 3;
                baseHp = 5;
                baseCooldown = 1200 - (levelIndex * 50);
                if (baseCooldown < 400) baseCooldown = 400;
                break;

            case "Kondoria":
                baseSpeed = 1;
                baseHp = 4 + (levelIndex * 2);
                baseCooldown = 1000 - (levelIndex * 60);
                if (baseCooldown < 300) baseCooldown = 300;
                break;

            case "Boss":
                baseSpeed = 1;
                baseHp = 25 + (levelIndex * 10) + (numPlayers * 5);
                basePower = 0;
                baseCooldown = Math.max(3000, 6000 - (levelIndex * 500));
                break;
        }

        int finalSpeed = (int) Math.max(1, baseSpeed * (levelIndex >= 4 ? playerMultiplier : 1));
        int finalHp = (int) (baseHp * totalMultiplier);
        int finalPower = basePower;
        long finalCooldown = (long) (baseCooldown / playerMultiplier);

        double x = tileX * TILE_SIZE;
        double y = tileY * TILE_SIZE;

        Mob bot = switch (type) {
            case "Doll" -> new DollBot(x, y, TILE_SIZE, TILE_SIZE, finalSpeed, finalHp, finalPower, finalCooldown, target, currentGrid);
            case "Kondoria" -> new KondoriaBot(x, y, TILE_SIZE, TILE_SIZE, finalSpeed, finalHp, finalPower, finalCooldown, allPlayers, currentGrid);
            case "Boss" -> new BossBot(x, y, TILE_SIZE, TILE_SIZE, finalSpeed, finalHp, finalCooldown, currentGrid);
            default -> null;
        };

        if (bot != null) {
            Mobs.add(bot);
        }
    }
}