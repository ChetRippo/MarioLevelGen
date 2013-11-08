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

    Random randomking = new Random();
    int chunkWidth = randomking.nextInt(6) + 3;
    static final int floor = (int)Math.floor(Math.random()*5) + 10;

    static int chunksToMake = (int)Math.floor(Math.random()*30)+10;

    public int[][] sketchedLevel = new int[8][30];

    public BlazeLevel(int width, int height, long seed, int difficulty,
                      int type) {
        super(width, height);
        create(seed, difficulty, type);
    }

    private void create(long seed, int difficulty, int type) {

        double block_density = 0.7; //Between 0 - 1
        int platform_avg = 1;
        ChunkBuilder c = new ChunkBuilder(this, block_density, platform_avg);

        buildStartChunk(c);

        buildChunk(c, chunkWidth, 'p');

    }

    private char getChunkType(double rand, char currentType){
        if(rand > 0.6){
            if(currentType == 'n'){
                if(rand > 0.7){
                    return 'p';
                }else{
                    return 'q';
                }
            }else{
                if(rand > 0.7){
                    return 'n';
                }else{
                    return 'q';
                }
            }
        }else{
            return currentType;
        }
    }

    private void buildChunk(ChunkBuilder c, int startX, char type) {
        int chunkFloor = (floor - 3 - (int)Math.floor(Math.random()*4));
        int[][] chunk = c.buildChunks(startX, chunkFloor, chunkWidth, chunkWidth, type);

        addChunkToSketchedLevel(chunk);

        //Random generator = new Random();
        //int i = generator.nextInt(10) + 1;
        c.block_density = Math.random()*1;

        chunksToMake--;

        if(chunksToMake == 0){
            buildFinalChunk(c, startX + chunkWidth);
        }else{
            buildChunk(c, startX+chunkWidth, getChunkType(Math.random(), type));
        }
    }

    private void buildStartChunk(ChunkBuilder c){
        int[][] chunk = c.buildChunks(0, floor-3, chunkWidth, chunkWidth, 'n');

        addChunkToSketchedLevel(chunk);
    }

    private void buildFinalChunk(ChunkBuilder c, int startX){
        xExit = startX+4;
        yExit = floor-3;
        int[][] chunk = c.buildChunks(startX, floor-3, chunkWidth, chunkWidth, 'n');

        addChunkToSketchedLevel(chunk);
    }

    private void addChunkToSketchedLevel(int[][] chunk){
        int[][] newSketchedLevel = new int[sketchedLevel.length+chunk.length][30];
        System.arraycopy(sketchedLevel, 0, newSketchedLevel, 0, sketchedLevel.length);
        System.arraycopy(chunk, 0, newSketchedLevel, sketchedLevel.length, chunk.length);
        sketchedLevel = newSketchedLevel;
    }
}
