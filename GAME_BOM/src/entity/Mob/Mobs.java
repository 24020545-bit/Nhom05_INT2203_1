package entity.Mob;


import java.util.ArrayList;
import java.util.List;

public class Mobs {
    public static final List<Mob> MOBS = new ArrayList<>();

    public static void add(Mob mob) {
        if (mob != null) {
            MOBS.add(mob);
        }
    }

    public static void remove(Mob mob) {
        MOBS.remove(mob);
    }

    public static void clear() {
        MOBS.clear();
    }
}
