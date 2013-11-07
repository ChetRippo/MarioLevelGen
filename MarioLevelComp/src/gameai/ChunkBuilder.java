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

    public void buildChunks(int startX, int startY, int width, int height, char type) {
        if (width <= 0) {throw new IllegalArgumentException("buildChunks Exception : Need positive width");}
        if (height <= 0) {throw new IllegalArgumentException("buildChunks Exception : Need positive height");}

        //Build a 2D array of width by height
        int[][] chunk = new int[width][height];

        //n - normal, flatish terrain
        //q - question blocks everywhere
        //p - small platforms

        switch(type){
            case 'n':
                this.block_density = Math.random()*1.1;
                this.platform_size = 9;
                this.pillars = 0.2;
                break;
            case 'p':
                this.block_density = Math.random()*0.25;
                this.platform_size = 2;
                this.pillars = 0.01;
                break;
        }

        sketchChunk(chunk, width, height); //populates array with 0's and 1's for blocks
        setChunk(chunk, width, height, startX, startY, type); //set the actual tiles for the chunk
        setBelowChunk(chunk, width, height, startX, startY, type);// populate tiles under a chunk (I figured I'd make this separate since it chunks have different types the area under them should change)
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

    private void setChunk(int[][] chunk, int width, int height, int startX, int startY, char type) {
        //Traverse each row of the array:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                //paint each block appropriately
                if(chunk[x][y] == 1) {
                    /*
                    * Build an array that will keep track of the position of
                    * the block relative to the whole chunk, so we can decorate it
                    * as top-left, or floating, or ground etc
                    *
                    * [TOP | FLOATING | LEFT | RIGHT]
                    * */
                    boolean[] checks = new boolean[4];
                    //if top block
                    checks[0] = checkBlockTop(chunk, x, y);
                    //if floating box
                    checks[1] = checkBlockFloat(chunk, x, y, height);
                    //if left wall
                    checks[2] = checkBlockLeft(chunk, x, y);
                    //if right wall
                    checks[3] = checkBlockRight(chunk, x, y, width);
                    checkChunkChecks(checks, x, startX, y, startY, type);
                }
            }
        }
    }

    private void setBelowChunk(int[][]chunk, int width, int height, int startX, int startY, char type){
        for(int x = startX; x < startX+width; x++){
            boolean set = false;
            for(int y = startY+height-2; y < 23; y++){
                if(lvl.getBlock(x, y) != BlazeLevel.Tiles.BLOCK_EMPTY && lvl.getBlock(x, y) != BlazeLevel.Tiles.COIN && lvl.getBlock(x, y) != BlazeLevel.Tiles.BLOCK_COIN && lvl.getBlock(x, y) != BlazeLevel.Tiles.BLOCK_POWERUP && lvl.getBlock(x, y) != 0){
                    set = true;
                }
                if(set && lvl.getBlock(x, y) == 0){
                    if(type == 'n'){
                        lvl.setBlock(x, y, BlazeLevel.Tiles.GROUND);
                    }else if(type == 'p'){
                        lvl.setBlock(x, y, BlazeLevel.Tiles.ROCK);
                    }
                }
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
        boolean beneath_ledge = checkUnderLedge(chunk, width, height, x, y);
        if (beneath_ledge) {block_chance += 0.3;}
        return block_chance;
    }

    private boolean checkUnderLedge(int[][] chunk, int width, int height, int x, int y) {
        if ((x == 0 || x == width - 1) || y <= jumpHeight) {return false;}
        boolean block_gap = (chunk[x-1][y-jumpHeight] == 1) && (chunk[x][y-jumpHeight] == 0);
        boolean gap_block = (chunk[x+1][y-jumpHeight] == 1) && (chunk[x][y-jumpHeight] == 0);
        return block_gap || gap_block;
    }

    private void checkChunkChecks(boolean[] checks, int x, int startX, int y, int startY, char type) {

        if(type == 'p'){
            lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.ROCK);
        }

        //top checks
        else if(checks[0]) {
            //float
            if(checks[1]) {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.BLOCK_COIN);
            }
            //top single (top of a pillar
            else if (checks[2] && checks[3]) {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.ROCK);
            }
            //top left
            else if (checks[2]) {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.LEFT_UP_GRASS_EDGE);
            }
            //top right
            else if(checks[3]){
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.RIGHT_UP_GRASS_EDGE);
            }else{
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.HILL_TOP);
            }
        }
        else {
            //if floating
            if (checks[1]) {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.COIN); //coin for fun
            }
            //middle left
            else if (checks[2]) {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.LEFT_GRASS_EDGE);
            }
            //middle right
            else if (checks[3]) {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.RIGHT_GRASS_EDGE);
            }
            //center, unreachable
            else {
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.GROUND);
            }
        }
    }

    private boolean checkBlockTop(int[][] chunk, int x, int y) {
        if (y == 0 || y > 20) {return true;}
        if (y > 0) {
            if(chunk[x][y-1] != 1) {return true;}
        }
        return false;
    }

    private boolean checkBlockFloat(int[][] chunk, int x, int y, int height) {
        if (y < height - 3) {
            if(chunk[x][y+1] != 1 && chunk[x][y+2] != 1 && chunk[x][y+3] != 1)  {return true;}
        }
        return false;
    }

    private boolean checkBlockLeft(int[][] chunk, int x, int y) {
        if(x == 0) {return true;}
        if(x > 0) {
            try{if(chunk[x-1][y] != 1 && !(chunk[x][y+1] != 1 && chunk[x][y-1] !=1)) {return true;}}
            catch(ArrayIndexOutOfBoundsException z){
                return false;
            }
        }
        return false;
    }
    private boolean checkBlockRight(int[][] chunk, int x, int y, int width) {
        if(x < width -1) {
            if(chunk[x+1][y] != 1){return true;}
        }
        return false;
    }
}
