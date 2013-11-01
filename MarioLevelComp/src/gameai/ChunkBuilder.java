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
    Level lvl;

    public ChunkBuilder(Level lvl) {
        this.lvl = lvl;
    }

    public void buildChunks(int startX, int startY, int width, int height, byte block) {
        if (width <=0) {throw new IllegalArgumentException("buildChunks Exception : Need positive width");}
        if (height <=0) {throw new IllegalArgumentException("buildChunks Exception : Need positive height");}

        //What is the middle, for placing start and end platforms
        int center_block = height / 2;

        //Build a 2D array of width by height
        int[][] chunk = new int[width][height];

        //Traverse each row of the array:
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                //When was the last block placed? What are our chances to dropping a block?
                //Is there a ledge (block->gap or gap->ledge) above us?
                lvl.setBlock(x+startX, y+startY, block);
            }
        }
    }

}
