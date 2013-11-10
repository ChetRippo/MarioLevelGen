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
    int chunkWidth = fate.nextInt(18) + 10; //chunks of width 10 -> 28
    int chunkHeight = fate.nextInt(8) + 3; //chunks of height 3 -> 11
    static final int WORLD_HEIGHT = 14;
    static final int JUMP_HEIGHT = 4;

    private static int totalChunks = 0;

    public BlazeLevel(int width, int height, long seed, int difficulty,
                      int type) {
        super(width, height);
        create();
    }

    /*
        Create the level
     */
    private void create() {

        double block_density = 0.5; //Between 0 - 1
        int platform_avg = 4; //approximate average of continuous blocks in a row
        ChunkBuilder c = new ChunkBuilder(this, block_density, platform_avg);

        buildStartChunk(c); //build the first chunk in the level
        buildChunk(c, chunkWidth, 'p'); //build the next chunk of type 'p'
    }

    /*
        Get the next type based on what the current type is and randomness
     */
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

    /*
        Build a single chunk in the world
     */
    private void buildChunk(ChunkBuilder c, int startX, char type) {
        int startY = WORLD_HEIGHT - chunkHeight;
        c.buildChunks(startX, startY, chunkWidth, chunkHeight, type);
        System.out.printf("BLOCK DENS IS %f\n", c.block_density);
        c.block_density = Math.random()*1;

        int curr_width = chunkWidth;
        int curr_height = chunkHeight; //buildChunks returns the highest exit point at the end of the chunk
        chunkWidth = fate.nextInt(18) + 10; //chunks of width 10 -> 28
        chunkHeight = fate.nextInt(2 * JUMP_HEIGHT) + (curr_height - JUMP_HEIGHT);//make next chunk height in jumpable range
        //System.out.printf("LAST HEIGHT WAS %d NEXT HEIGHT IS %d\n", curr_height, chunkHeight);
        if (chunkHeight < 3) {chunkHeight = 3;}
        else if (chunkHeight > 11) {chunkHeight = 11;}
        totalChunks++;
        if(Math.random()*totalChunks > 4){
            buildFinalChunk(c, startX + curr_width);
        }else{
            buildChunk(c, startX+curr_width+2, getChunkType(Math.random(), type));
        }
    }

    private void buildStartChunk(ChunkBuilder c){
        int chunk = c.buildChunks(0, WORLD_HEIGHT-chunkHeight, chunkWidth, chunkHeight, 'n');

        //addChunkToSketchedLevel(chunk);
    }

    private void buildFinalChunk(ChunkBuilder c, int startX){
        xExit = startX;
        yExit = WORLD_HEIGHT-chunkHeight;
        int chunk = c.buildChunks(startX+2, WORLD_HEIGHT-chunkHeight, chunkWidth, chunkHeight, 'n');

        //addChunkToSketchedLevel(chunk);
    }
    /*
    private void addChunkToSketchedLevel(int[][] chunk){
        int[][] newSketchedLevel = new int[sketchedLevel.length+chunk.length][30];
        System.arraycopy(sketchedLevel, 0, newSketchedLevel, 0, sketchedLevel.length);
        System.arraycopy(chunk, 0, newSketchedLevel, sketchedLevel.length, chunk.length);
        sketchedLevel = newSketchedLevel;
    } */
}
