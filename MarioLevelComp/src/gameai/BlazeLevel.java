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
 * User: weed
 * Date: 10/30/13
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */

// SCF
//C -> CC || CF

public class BlazeLevel extends Level implements LevelInterface {
    static class Tiles{
        public static final byte ROCK = BlazeLevel.ROCK;
        public static final byte GROUND = BlazeLevel.GROUND;

        public static final byte HILL_TOP = BlazeLevel.HILL_TOP;
        public static final byte HILL_TOP_LEFT = BlazeLevel.HILL_TOP_LEFT;
        public static final byte HILL_TOP_LEFT_IN = BlazeLevel.HILL_TOP_LEFT_IN;
        public static final byte HILL_TOP_RIGHT = BlazeLevel.HILL_TOP_RIGHT;
        public static final byte HILL_TOP_RIGHT_IN = BlazeLevel.HILL_TOP_RIGHT_IN;
        public static final byte HILL_FILL = BlazeLevel.HILL_FILL;
        public static final byte HILL_LEFT = BlazeLevel.HILL_LEFT;
        public static final byte HILL_RIGHT = BlazeLevel.HILL_RIGHT;

        public static final byte LEFT_GRASS_EDGE = BlazeLevel.LEFT_GRASS_EDGE;
        public static final byte LEFT_POCKET_GRASS = BlazeLevel.LEFT_POCKET_GRASS;
        public static final byte LEFT_UP_GRASS_EDGE = BlazeLevel.LEFT_UP_GRASS_EDGE;
        public static final byte RIGHT_GRASS_EDGE = BlazeLevel.RIGHT_GRASS_EDGE;
        public static final byte RIGHT_POCKET_GRASS = BlazeLevel.RIGHT_POCKET_GRASS;
        public static final byte RIGHT_UP_GRASS_EDGE = BlazeLevel.RIGHT_UP_GRASS_EDGE;

        public static final byte BLOCK_COIN = BlazeLevel.BLOCK_COIN;
        public static final byte BLOCK_EMPTY = BlazeLevel.BLOCK_EMPTY;
        public static final byte BLOCK_POWERUP = BlazeLevel.BLOCK_POWERUP;
        public static final byte COIN = BlazeLevel.COIN;

        public static final byte TUBE_TOP_LEFT = BlazeLevel.TUBE_TOP_LEFT;
        public static final byte TUBE_TOP_RIGHT = BlazeLevel.TUBE_TOP_RIGHT;
        public static final byte TUBE_SIDE_LEFT = BlazeLevel.TUBE_SIDE_LEFT;
        public static final byte TUBE_SIDE_RIGHT = BlazeLevel.TUBE_SIDE_RIGHT;
    }

    static final int chunkWidth = 8;
    static final int floor = (int)Math.floor(Math.random()*10) + 7;

    public BlazeLevel(int width, int height, long seed, int difficulty,
                      int type) {
        super(width, height);
        create(seed, difficulty, type);
    }

    private void create(long seed, int difficulty, int type) {

        double block_density = 0.7; //Between 0 - 1
        ChunkBuilder c = new ChunkBuilder(this, block_density, 1);

        buildStartChunk(c);

        buildChunk(c, chunkWidth);

    }

    private void buildChunk(ChunkBuilder c, int startX) {
        c.buildChunks(startX, floor-3-(int)Math.floor(Math.random()*4), chunkWidth, chunkWidth);

        c.block_density = Math.random()*1;

        int isLastChunk = (int)Math.floor(Math.random()*startX);
        boolean last = (isLastChunk > 60);
        if(last){
            buildFinalChunk(c, startX + chunkWidth);
        }else{
            buildChunk(c, startX+chunkWidth);
        }
    }

    private void buildStartChunk(ChunkBuilder c){
        c.buildChunks(0, floor-3, chunkWidth, chunkWidth);
    }

    private void buildFinalChunk(ChunkBuilder c, int startX){
        xExit = startX+4;
        yExit = floor-3;
        c.buildChunks(startX, floor-3, chunkWidth, chunkWidth);
    }

    private void decorate(int xStart, int xLength, int floor) {
        //if its at the very top, just return
        if (floor < 1)
            return;
        boolean rocks = true;

        int s = (int)Math.floor(Math.random()*4)+1;
        int e = (int)Math.floor(Math.random()*4)+1;

        if (floor - 2 > 0) {
            if ((xLength - 1 - e) - (xStart + 1 + s) > 1) {
                for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
                    setBlock(x, floor - 2, (byte) (2 + 2 * 16));
                }
            }
        }

        s = (int)Math.floor(Math.random()*4)+1;
        e = (int)Math.floor(Math.random()*4)+1;

        if (floor - 4 > 0) {
            if ((xLength - 1 - e) - (xStart + 1 + s) > 2) {
                for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
                    if (rocks) {
                        if (x != xStart + 1 && x != xLength - 2 &&
                                Math.floor(Math.random()*2) == 0) {
                            if (Math.floor(Math.random()*2) == 0) {
                                setBlock(x, floor - 4, BLOCK_POWERUP);
                            } else {
                                setBlock(x, floor - 4, BLOCK_EMPTY);
                            }
                        } else if (Math.floor(Math.random()*4) == 0) {
                            if (Math.floor(Math.random()*4) == 0) {
                                setBlock(x, floor - 4, (byte) (2 + 1 * 16));
                            } else {
                                setBlock(x, floor - 4, (byte) (1 + 1 * 16));
                            }
                        } else {
                            setBlock(x, floor - 4, BLOCK_EMPTY);
                        }
                    }
                }
            }
        }
    }

}
