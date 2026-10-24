//package Player;
//
//import Map.GameMap;
//
//public class SpeedBot extends Bot {
//    public SpeedBot(int x, int y, double speed, int lives, int bombLimit, int power) {
//        super(x, y, speed, lives, bombLimit, power);
//    }
//
//    @Override
//    public void updateAI(GameMap gameMap) {
//        if (!isAlive) return;
//        if (Math.random() < 0.25) {
//            Direction[] dirs = Direction.values();
//            this.direction = dirs[(int) (Math.random() * dirs.length)];
//        }
////        move(this.direction, gameMap);
//    }
//}