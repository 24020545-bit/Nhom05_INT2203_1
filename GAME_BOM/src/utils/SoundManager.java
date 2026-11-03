package utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundManager {

    // Hàm phát âm thanh hiệu ứng (chạy trên luồng phụ để không làm lag game)
    public static void playSoundEffect(String soundFilePath) {
        new Thread(() -> {
            try {
                URL url = SoundManager.class.getResource(soundFilePath);
                if (url != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                } else {
                    System.err.println("Không tìm thấy file âm thanh: " + soundFilePath);
                }
            } catch (Exception e) {
                System.err.println("Lỗi phát âm thanh hiệu ứng: " + e.getMessage());
            }
        }).start();
    }
}