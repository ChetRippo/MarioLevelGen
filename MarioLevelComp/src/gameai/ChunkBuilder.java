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
 * User: ericpeterson
 * Date: 11/1/13
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */

/*
 Values Important for Chunk Generation
 - Length : changes how many chunks are made
 - Block Density : changes number of blocks in a chunk
 - Platform Average : What is the "average" number of contiguous blocks
 - Pillars : changes the chances of a placed block being designated as the top of a column
 - Floor : changes the chances of any given row being the "top" of the floor (aka all beneath it are filled in)
 */

//A chunk builder object is created to spit out chunks for a level
public class ChunkBuilder {

    //members
    int jumpHeight = 3; //This is used to determine the max height blocks can be spaced vertically
    Level lvl;
    double block_density;
    int platform_size;
    double pillars = 0.2;
    double floor = 0.1;

    public ChunkBuilder(Level lvl, double block_density, int platform_size) {
        this.lvl = lvl;
        this.block_density = block_density;
        this.platform_size =  platform_size; //what is the "average" platform size
    }

    public void buildChunks(int startX, int startY, int width, int height) {
        if (width <= 0) {throw new IllegalArgumentException("buildChunks Exception : Need positive width");}
        if (height <= 0) {throw new IllegalArgumentException("buildChunks Exception : Need positive height");}

        //Build a 2D array of width by height
        int[][] chunk = new int[width][height];

        sketchChunk(chunk, width, height); //populates array with 0's and 1's for blocks
        setChunk(chunk, width, height, startX, startY); //set the actual tiles for the chunk
    }

    public void sketchChunk(int[][] chunk, int width, int height) {
        //What is the middle, for placing start and end platforms
        int center_block = height / 2;
        double block_chance = 1.0; //how likely are we to place a block
        int continuous_blocks = 0; //Did we just place a block

        chunk[0][center_block] = 1;
        chunk[width - 1][center_block] = 1;
        int[] columns = new int[width];
        int floor_top = height+1; //just a junk value

        //Traverse each row of the array:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                if (y > floor_top || columns[x] == 1) {
                    chunk[x][y] = 1;
                }
                else {
                    //When was the last block placed? What are our chances to dropping a block?
                    block_chance = calculateBlockChance(chunk, continuous_blocks, width, height, x, y);
                    double block_picker = Math.random();
                    //Is our picker within block_chance
                    if(block_picker < block_chance) {
                        chunk[x][y] = 1;
                        continuous_blocks++;

                        if (block_picker < pillars) {
                            chunk[x][y] = 1;
                            columns[x] = 1;
                        }

                        if (block_picker < floor) {
                            floor = y;
                        }
                    }
                    else {continuous_blocks = 0;}
                }
            }
        }
    }

    private void setChunk(int[][] chunk, int width, int height, int startX, int startY) {
        //Traverse each row of the array:
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                if(chunk[x][y] == 1) {lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.HILL_TOP);}
            }
        }
    }

    private double calculateBlockChance(int[][] chunk, int continuous_blocks, int width, int height, int x, int y) {
        double block_chance = 0.0;
        if (continuous_blocks < platform_size) {
            block_chance = block_density + (continuous_blocks * 0.2);
        }
        else {
            block_chance = block_density - (continuous_blocks * 0.2);
        }
        //Is there a ledge (block->gap or gap->ledge) above us?
        boolean beneath_ledge = underLedgeCheck(chunk, width, height, x, y);
        if (beneath_ledge) {block_chance += 0.3;}
        return block_chance;
    }

    private boolean underLedgeCheck(int[][] chunk, int width, int height, int x, int y) {
        if ((x == 0 || x == width - 1) || y <= jumpHeight) {return false;}
        boolean block_gap = (chunk[x-1][y-jumpHeight] == 1) && (chunk[x][y-jumpHeight] == 0);
        boolean gap_block = (chunk[x+1][y-jumpHeight] == 1) && (chunk[x][y-jumpHeight] == 0);
        return block_gap || gap_block;
    }

}
