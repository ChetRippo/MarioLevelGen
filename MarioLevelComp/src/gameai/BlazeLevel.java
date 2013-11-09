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

    Random fate = new Random();
    int chunkWidth = fate.nextInt(18) + 10;
    static final int floor = (int)Math.floor(Math.random()*5) + 10;

    private static int totalChunks = 0;
    public int[][] sketchedLevel = new int[8][30];

    public BlazeLevel(int width, int height, long seed, int difficulty,
                      int type) {
        super(width, height);
        create(seed, difficulty, type);
    }

    private void create(long seed, int difficulty, int type) {

        double block_density = 0.7; //Between 0 - 1
        int platform_avg = 4;
        ChunkBuilder c = new ChunkBuilder(this, block_density, platform_avg);

        buildStartChunk(c);

        buildChunk(c, chunkWidth, 'p');

    }

    private char getChunkType(double rand, char currentType){
        if(rand > 0.6){
            if(currentType == 'n'){
                    return 'p';
            }else{
                    return 'n';
            }
        }else{
            return currentType;
        }
    }

    private void buildChunk(ChunkBuilder c, int startX, char type) {
        int chunkFloor = (floor - 3 - (int)Math.floor(Math.random()*4));
        int[][] chunk = c.buildChunks(startX, chunkFloor, chunkWidth, (22-floor), type);

        addChunkToSketchedLevel(chunk);

        //Random generator = new Random();
        //int i = generator.nextInt(10) + 1;
        c.block_density = Math.random()*1;

        int curr_width = chunkWidth;
        chunkWidth = fate.nextInt(10) + 3;
        totalChunks++;
        if(Math.random()*totalChunks > 15){
            buildFinalChunk(c, startX + curr_width);
        }else{
            buildChunk(c, startX+curr_width+2, getChunkType(Math.random(), type));
        }
    }

    private void buildStartChunk(ChunkBuilder c){
        int[][] chunk = c.buildChunks(0, floor-3, chunkWidth, (22-floor), 'n');

        addChunkToSketchedLevel(chunk);
    }

    private void buildFinalChunk(ChunkBuilder c, int startX){
        xExit = startX;
        yExit = floor-3;
        int[][] chunk = c.buildChunks(startX+2, floor-3, chunkWidth, (22-floor), 'n');

        addChunkToSketchedLevel(chunk);
    }

    private void addChunkToSketchedLevel(int[][] chunk){
        int[][] newSketchedLevel = new int[sketchedLevel.length+chunk.length][30];
        System.arraycopy(sketchedLevel, 0, newSketchedLevel, 0, sketchedLevel.length);
        System.arraycopy(chunk, 0, newSketchedLevel, sketchedLevel.length, chunk.length);
        sketchedLevel = newSketchedLevel;
    }
}
