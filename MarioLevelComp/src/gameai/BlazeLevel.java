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
        int platform_avg = 1;
        ChunkBuilder c = new ChunkBuilder(this, block_density, 1);

        buildStartChunk(c);

        buildChunk(c, chunkWidth);

    }

    private void buildChunk(ChunkBuilder c, int startX) {
        int chunkFloor = (floor - 3 - (int)Math.floor(Math.random()*4));
        c.buildChunks(startX, chunkFloor, chunkWidth, chunkWidth);

        c.block_density = Math.random()*1;

        int isLastChunk = (int)Math.floor(Math.random()*startX);
        boolean last = (isLastChunk > 60);
        if(last){
            buildFinalChunk(c, startX + chunkWidth);
        }else{
            buildChunk(c, startX+chunkWidth);
        }

        //decorate(startX, 30, (int)Math.floor(Math.random()*12));
    }

    private void buildStartChunk(ChunkBuilder c){
        c.buildChunks(0, floor-3, chunkWidth, chunkWidth);
    }

    private void buildFinalChunk(ChunkBuilder c, int startX){
        xExit = startX+4;
        yExit = floor-3;
        c.buildChunks(startX, floor-3, chunkWidth, chunkWidth);
        fixWalls();
    }

    private void fixWalls() {
        boolean[][] blockMap = new boolean[width + 1][height + 1];

        for (int x = 0; x < width + 1; x++) {
            for (int y = 0; y < height + 1; y++) {
                int blocks = 0;
                for (int xx = x - 1; xx < x + 1; xx++) {
                    for (int yy = y - 1; yy < y + 1; yy++) {
                        if (getBlockCapped(xx, yy) == GROUND) {
                            blocks++;
                        }
                    }
                }
                blockMap[x][y] = blocks == 4;
            }
        }
        blockify(this, blockMap, width + 1, height + 1);
    }

    private void blockify(Level level, boolean[][] blocks, int width,
                          int height) {
        int to = 0;

        boolean[][] b = new boolean[2][2];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int xx = x; xx <= x + 1; xx++) {
                    for (int yy = y; yy <= y + 1; yy++) {
                        int _xx = xx;
                        int _yy = yy;
                        if (_xx < 0) _xx = 0;
                        if (_yy < 0) _yy = 0;
                        if (_xx > width - 1) _xx = width - 1;
                        if (_yy > height - 1) _yy = height - 1;
                        b[xx - x][yy - y] = blocks[_xx][_yy];
                    }
                }

                if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
                    if (b[0][0] == b[0][1]) {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        } else {
                            // KEEP OLD BLOCK!
                        }
                    } else {
                        if (b[0][0]) {
                            //down grass top?
                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                        } else {
                            //up grass top
                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                        }
                    }
                } else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
                    if (b[0][0]) {
                        //right grass top
                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                    } else {
                        //left grass top
                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                    }
                } else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                } else if (b[0][0] == b[1][0]) {
                    if (b[0][0]) {
                        if (b[0][1]) {
                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                        }
                    } else {
                        if (b[0][1]) {
                            //right up grass top
                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                        } else {
                            //left up grass top
                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                        }
                    }
                } else if (b[0][1] == b[1][1]) {
                    if (b[0][1]) {
                        if (b[0][0]) {
                            //left pocket grass
                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                        } else {
                            //right pocket grass
                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                        }
                    } else {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                        }
                    }
                } else {
                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                }
            }
        }
    }
}
