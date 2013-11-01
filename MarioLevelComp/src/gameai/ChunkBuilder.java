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

//A chunk builder object is created to spit out chunks for a level
public class ChunkBuilder {

    //members
    int jumpHeight = 3; //This is used to determine the max height blocks can be spaced vertically
    double block_density;
    Level lvl;

    public ChunkBuilder(Level lvl, double block_density) {
        this.lvl = lvl;
        this.block_density = block_density;
    }

    public void buildChunks(int startX, int startY, int width, int height, byte block) {
        if (width <=0) {throw new IllegalArgumentException("buildChunks Exception : Need positive width");}
        if (height <=0) {throw new IllegalArgumentException("buildChunks Exception : Need positive height");}

        //What is the middle, for placing start and end platforms
        int center_block = height / 2;

        //Build a 2D array of width by height
        int[][] chunk = new int[width][height];

        sketchChunk(chunk, width, height); //populates array with 0's and 1's for blocks
        setChunk(chunk, width, height, startX, startY, block);
    }

    public void sketchChunk(int[][] chunk, int width, int height) {

        //Traverse each row of the array:
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                //When was the last block placed? What are our chances to dropping a block?
                double block_chance = calculateBlockChance();
                //Is there a ledge (block->gap or gap->ledge) above us?
                //boolean beneath_ledge = underLedgeCheck();
                double picker = Math.random();
                if(picker < block_density) {chunk[x][y] = 1;}
            }
        }
    }

    public void setChunk(int[][] chunk, int width, int height, int startX, int startY, byte block) {
        //Traverse each row of the array:
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                if(chunk[x][y] == 1) {lvl.setBlock(x+startX, y+startY, block);}
            }
        }
    }

    public double calculateBlockChance() {
        return 0.0;
    }

}
