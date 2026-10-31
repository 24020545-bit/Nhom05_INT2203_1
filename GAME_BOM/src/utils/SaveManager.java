package utils;

import entity.Player;
import entity.mob.Mob;
import entity.mob.Mobs;
import entity.bot.DollBot;
import entity.bot.KondoriaBot;

import java.io.*;
import java.util.Properties;

public class SaveManager {
    private static final String SAVE_FILE = "save.properties";
    private static int unlockedLevel = 1;

    public static void loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            props.load(input);
            String lvlStr = props.getProperty("unlockedLevel");
            if (lvlStr != null) {
                unlockedLevel = Integer.parseInt(lvlStr);
                ui.screen.LevelSelectScreen.setUnlockedLevel(unlockedLevel);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi load game: " + e.getMessage());
        }
    }

    public static void saveGame(int level) {
        unlockedLevel = Math.max(unlockedLevel, level);
        Properties props = new Properties();
        props.setProperty("unlockedLevel", String.valueOf(unlockedLevel));

        try (OutputStream output = new FileOutputStream(SAVE_FILE)) {
            props.store(output, "Bomberman Save Data");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu game: " + e.getMessage());
        }
    }

    // LƯU TRẠNG THÁI CHI TIẾT: PLAYER, MAP GRID VÀ TẤT CẢ BOT
    public static void saveGameProgress(int currentLevel, Player player, int[][] grid) {
        Properties props = new Properties();
        props.setProperty("unlockedLevel", String.valueOf(unlockedLevel));
        props.setProperty("currentLevel", String.valueOf(currentLevel));

        // 1. Lưu thông số Player
        props.setProperty("playerX", String.valueOf(player.getX()));
        props.setProperty("playerY", String.valueOf(player.getY()));
        props.setProperty("playerHp", String.valueOf(player.getHp()));
        props.setProperty("playerPower", String.valueOf(player.getPower()));
        props.setProperty("playerSpeed", String.valueOf(player.getSpeed()));

        // 2. Lưu trạng thái bản đồ (Mảng 2 chiều grid chuyển thành chuỗi phân tách bởi dấu phẩy)
        StringBuilder gridStr = new StringBuilder();
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                gridStr.append(grid[y][x]);
                if (!(y == grid.length - 1 && x == grid[y].length - 1)) {
                    gridStr.append(",");
                }
            }
        }
        props.setProperty("mapGrid", gridStr.toString());

        // 3. Lưu danh sách Bot còn sống
        int botCount = 0;
        for (Mob mob : Mobs.MOBS) {
            if (!(mob instanceof Player) && !(mob instanceof item.Item) && mob.isSurvival()) {
                String className = mob.getClass().getName();
                props.setProperty("bot_" + botCount + "_type", className);
                props.setProperty("bot_" + botCount + "_x", String.valueOf(mob.getX()));
                props.setProperty("bot_" + botCount + "_y", String.valueOf(mob.getY()));
                props.setProperty("bot_" + botCount + "_hp", String.valueOf(mob.getHp()));
                botCount++;
            }
        }
        props.setProperty("botCount", String.valueOf(botCount));

        try (OutputStream output = new FileOutputStream(SAVE_FILE)) {
            props.store(output, "Bomberman In-Game Full State Save");
            System.out.println("Đã lưu toàn bộ trạng thái màn chơi (Map + Bot + Player)!");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu game dở: " + e.getMessage());
        }
    }

    public static void clearSaveFile() {
        saveGame(unlockedLevel);
    }

    public static Properties loadGameProgress() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return null;

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            props.load(input);
            if (!props.containsKey("currentLevel")) return null;
            return props;
        } catch (IOException e) {
            return null;
        }
    }
}