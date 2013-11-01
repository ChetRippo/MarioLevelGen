package gameai;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.Level;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Butt
 * Date: 10/30/13
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */

//butt comment
public class BlazeLevel extends Level implements LevelInterface {

    public BlazeLevel(int width, int height, long seed, int difficulty,
                      int type) {
        super(width, height);
        create(seed, difficulty, type);
    }

    public void create(long seed, int difficulty, int type) {
        for(int x = 0; x < width; x++){
            setBlock(x, 2, ROCK);
        }
        xExit = width-8;
    }

    /*private int buildZone(int x, int maxLength) {
    }

    private int buildJump(int xo, int maxLength) {

    }

    private int buildHillStraight(int xo, int maxLength) {

    }

    private void addEnemyLine(int x0, int x1, int y) {
    }

    private int buildTubes(int xo, int maxLength) {
    }

    private int buildStraight(int xo, int maxLength, boolean safe) {

    }

    private void decorate(int xStart, int xLength, int floor) {
    }

    private void fixWalls() {
    }

    private void blockify(Level level, boolean[][] blocks, int width,
                          int height) {
    } */

}
