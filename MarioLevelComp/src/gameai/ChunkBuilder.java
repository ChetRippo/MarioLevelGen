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
    Random fate = new Random();
    int jumpHeight = 4; //This is used to determine the max height blocks can be spaced vertically
    int jumpLength = 4;
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

    public int[][] buildChunks(int startX, int startY, int width, int height, char type) {
        if (width <= 0) {throw new IllegalArgumentException("buildChunks Exception : Need positive width");}
        if (height <= 0) {throw new IllegalArgumentException("buildChunks Exception : Need positive height");}

        //Build a 2D array of width by height
        //startY = 14;
        //width = 3;
        //height = 3;
        int[][] chunk = new int[width][height];

        //n - normal, flatish terrain
        //q - question blocks everywhere
        //p - small platforms

        switch(type){
            case 'n':
                this.block_density = Math.random();
                if(this.block_density < 0.2){
                    this.block_density = 0.2;
                }
                this.platform_size = (int)Math.floor(Math.random()*12)+4;
                this.pillars = 0.2;
                break;
            case 'p':
                this.block_density = Math.random()*0.25;
                this.platform_size = 2;
                this.pillars = 0.01;
                break;
            case 'q':
                this.block_density = Math.random();
                this.platform_size = 4;
                this.pillars = 0.7;
                break;

        }

        sketchChunk(chunk, width, height, type); //populates array with 0's and 1's for blocks
        setChunk(chunk, width, height, startX, startY, type); //set the actual tiles for the chunk
        setBelowChunk(chunk, width, height, startX, startY, type);// populate tiles under a chunk (I figured I'd make this separate since it chunks have different types the area under them should change)
        setEnemiesOnChunk(chunk, width, height, startX, startY, type);

        //add to level tiles
        return chunk;
    }

    public void sketchChunk(int[][] chunk, int width, int height, char type) {
        //What is the middle, for placing start and end platforms
        int center_block = height / 2;
        double block_chance = 0.0; //how likely are we to place a block
        int continuous_blocks = 0; //Did we just place a block
        int continuous_gap = 0;

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
                    if(block_picker < block_chance || continuous_gap > jumpLength) {
                        chunk[x][y] = 1;
                        continuous_blocks++;
                        continuous_gap = 0;

                        /*
                        if (block_picker < pillars) {
                            chunk[x][y] = 1;
                            columns[x] = 1;
                        }*/
                        if(type != 'p') {
                            columns[x] = 1;
                        }

                        if (block_picker < floor) {
                            floor = y;
                        }
                    }
                    else {
                        continuous_blocks = 0;
                        continuous_gap++;
                    }
                }
            }
        }
        //Given the solid block, dig out tunnels
        int tunnel_pass = fate.nextInt(2) + 2;
        while(tunnel_pass > 0) {
            digTunnels(chunk, width, height);
            tunnel_pass--;
        }
        //Now make sure all vertical jumps are feasable
        heightCritic(chunk, width, height);

        printChunkInfo(chunk, width, height);
    }

    /*
    Make 2 passes over a set chunk and dig tunnels (if possible)
    * */
    private void digTunnels(int[][] chunk, int width, int height) {
        boolean placedEntrance = false;
        boolean placedExit = false;
        int[] entrance = new int[2]; //[x,y] of entrance
        int[] exit = new int[2]; //[x,y] of exit
        //FIRST PASS : SEE IF THERE IS A LEGAL ENTRANCE AND EXIT
        //Traverse by column because that feels intuitive for the tunneling
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                    //check if its a good entrance spot
                    if(!placedEntrance && isLegalEntrance(x,y,chunk,width,height)) {
                        //if it is, place entrance[]
                        placedEntrance = true;
                        entrance[0] = x; entrance[1] = y;
                        System.out.printf("ENTRANCE MADE AT %d,%d\n",x,y);
                    }
                    //can we place an exit here
                    //check if its a good exit
                    else if(!placedExit && isLegalExit(x,y,chunk,width,height,entrance)) {
                        //if it is, place exit[]
                        placedExit = true;
                        exit[0] = x; exit[1] = y;
                        System.out.printf("EXIT MADE AT %d,%d\n",x,y);
                    }
            }
        }
        int tunnel_floor;
        if (entrance[1] < exit[1]) {//entrance is higher
            tunnel_floor = exit[1] + jumpHeight;
        }
        else {tunnel_floor = entrance[1] + jumpHeight;}
        if (tunnel_floor >= height) {tunnel_floor = height - 2;}

        //SECOND PASS : DIG A TUNNEL/CAVE BETWEEN THE TWO
        if (placedEntrance && placedExit) {
            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    boolean under_entrance = (x == entrance[0]) && (y >= entrance[1]);
                    boolean under_exit = (x == exit[0]) && (y >= exit[1]);
                    //if we are under an entrance or exit, DIG to tunnel floor
                    if((under_entrance || under_exit) && y < tunnel_floor) {
                        chunk[x][y] = 2; //may end up being 2 for painting purposes
                    }
                    else {
                        //lets see if its part of the tunnel between  exit and entrance
                        if ((x > entrance[0]) && (x < exit[0]) && (y < (tunnel_floor - 1))) {
                            //if there is a block above us, we can have a ceiling and its safe to dig
                            if((y > 0) && !isTopGround(chunk, x, y-1)) {
                                chunk[x][y] = 2; //may end up being 2 later
                            }
                        }

                    }
                }
            }
        }
        System.out.printf("Entrance: %b, Exit: %b, EnLoc: %d,%d, ExLox: %d,%d, floor: %d\n\n",
                placedEntrance, placedExit, entrance[1],entrance[1], exit[0],exit[1], tunnel_floor);
    }

    /*
     Pass over each column and make sure that the gap between the top of the next
     column and the "floor" of this column is no greater than jumpHeight

     If it is not, place a block (set to 1)
     */
    private void heightCritic(int[][] chunk, int width, int height) {
        for(int x=0; x < width - 1; x++) { //check all but second-to-last column
            int next_height = -1;
            int height_count = 0;
            for(int y=0; y < height; y++) {
                //if we haven't seen next height yet
                if(next_height == -1) {
                    if(chunk[x+1][y] == 1) {
                        next_height = y;
                    }
                }
                else {
                    height_count++;
                }

                //if it has been jumpHeight since last platform, force block
                if(height_count >= jumpHeight-1) {
                    //System.out.println("HEIGHT CRITIC SAID ENOUGH IS ENOUGH");
                    next_height = -1;
                    height_count = 0;
                    chunk[x][y] = 1;
                }
            }
        }
    }

    //is this spot a good place for a tunnel entrance
    private boolean isLegalEntrance(int x, int y, int[][] chunk, int width, int height) {
        boolean check = isTopGround(chunk,x,y) && !checkBlockFloat(chunk, x, y, height) && !checkBlockRight(chunk, x, y, width) && !checkBlockLeft(chunk, x, y);
        //boolean ground_top, ground_left, ground_right,
        //TODO:make it possible for first columns to be entrances, make tunnels shoot off front
        int threshold = 4;
        if (fate.nextInt(10) > threshold) {return check;}
        return false;
    }

    //is this spot a good place for a tunnel exit
    private boolean isLegalExit(int x, int y, int[][] chunk, int width, int height, int[] entrance) {
        boolean check = isTopGround(chunk, x, y) && !checkBlockFloat(chunk, x, y, height) && !checkBlockRight(chunk, x, y, width) && !checkBlockLeft(chunk, x, y);
        //TODO:make it possible for last columns to be entrances, make tunnels shoot off end
        int threshold = 4;
        if((x > entrance[0] + 2) && check) {
            //we can legally add a tunnel, but will fate allow it
            if (fate.nextInt(10) > threshold) {return true;}
        }
        return false;

    }

    //Specialized tunnels checks, because the standard checks dont conflict with desired goals
    private boolean isTopGround(int[][] chunk, int x, int y) {
        //first check that chunk is valid
        boolean check = chunk[x][y] == 1;
        if (check) {
            if(y > 0) {
                if (chunk[x][y-1] != 1) {return true;}
            }
            else {return true;}
        }
        return false;
    }

    ////PRINT OUT CHUNKS GOD DAMMIT
    private void printChunkInfo(int[][] chunk, int width, int height) {
        for(int y=0; y< height; y++) {
            System.out.print("[");
            for(int x=0; x<width; x++) {
                String out = "";
                if(chunk[x][y] == 0) {out = " ";}
                else if(chunk[x][y] == 2) {out = "∆";}
                else if(chunk[x][y] == 3) {out = "-";}
                else {out = "X";}
                if(x != width - 1) {out = out + "|";}
                System.out.print(out);
            }
            System.out.print("]\n");
        }
        System.out.print("\n");
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
                    * Each check true returns true if block is neighboring void
                    * above, below, or to the left or right of them
                    * making the block the topmost, floating, leftmost or rightmost block
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
                    //lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.ROCK); //test to see if checkChunkChecks is messing up
                    checkChunkChecks(checks, x, startX, y, startY, type); //uncomment to try and block for every 1

                }
                if(chunk[x][y] == 2){
                    lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.COIN);
                }
            }
        }
    }

    private void setBelowChunk(int[][]chunk, int width, int height, int startX, int startY, char type){
        if(type != 'q'){
            for(int x = startX; x < startX+width; x++){
                boolean set = false;
                for(int y = startY+height-2; y < 23; y++){
                    if(lvl.getBlock(x, y) != BlazeLevel.Tiles.BLOCK_EMPTY && lvl.getBlock(x, y) != BlazeLevel.Tiles.COIN && lvl.getBlock(x, y) != BlazeLevel.Tiles.BLOCK_COIN && lvl.getBlock(x, y) != BlazeLevel.Tiles.BLOCK_POWERUP && lvl.getBlock(x, y) != 0){
                        set = true;
                    }
                    if(set && lvl.getBlock(x, y) == 0){
                        if(type == 'n'){
                            if(lvl.getBlock(x-1, y) == 0){
                                lvl.setBlock(x, y, BlazeLevel.Tiles.LEFT_GRASS_EDGE);
                            }else if(lvl.getBlock(x+1, y) == 0){
                                lvl.setBlock(x, y, BlazeLevel.Tiles.RIGHT_GRASS_EDGE);
                                if(lvl.getBlock(x-1, y) == BlazeLevel.Tiles.RIGHT_GRASS_EDGE){
                                    lvl.setBlock(x-1, y, BlazeLevel.Tiles.GROUND);
                                }
                            }else{
                                lvl.setBlock(x, y, BlazeLevel.Tiles.GROUND);
                            }
                        }else if(type == 'p'){
                            lvl.setBlock(x, y, BlazeLevel.Tiles.ROCK);
                        }
                    }
                }
            }
        }
    }

    private void setEnemiesOnChunk(int[][] chunk, int width, int height, int startX, int startY, char type){
        if(startX > 24 && type == 'n' && Math.random() > 0.4){
            int enemy = 6;//(int)Math.floor(Math.random()*4);
            lvl.setSpriteTemplate(startX+(int)Math.floor(Math.random()*3), startY, new SpriteTemplate(enemy, true));
        }else if(startX > 24 && type == 'n'){
            for(int destX = startX;destX < width+startX; destX++){
                for(int destY = 6;destY < 22; destY++){
                    if(lvl.getBlock(destX, destY) == BlazeLevel.Tiles.COIN && lvl.getBlock(destX, destY+1) != BlazeLevel.Tiles.COIN){
                        lvl.setSpriteTemplate(destX, destY, new SpriteTemplate(Enemy.ENEMY_GOOMBA, false));
                        return;
                    }
                }
            }
        }if(startX > 24 && type == 'p' && Math.random() > 0.3){
            for(int destX = startX+(int)Math.floor(Math.random()*3);destX < width+startX; destX++){
                for(int destY = 6;destY < 22; destY++){
                    if(lvl.getBlock(destX, destY) == 0 && lvl.getBlock(destX, destY+1) == 0 &&lvl.getBlock(destX, destY+2) == 0 && lvl.getBlock(destX, destY+3) != 0 && lvl.getBlock(destX, destY+3) != BlazeLevel.Tiles.COIN && lvl.getBlock(destX+1, destY) == 0&& lvl.getBlock(destX-1, destY) == 0){
                        lvl.setBlock(destX, destY, (byte) (14 + 0 * 16));
                        lvl.setBlock(destX, destY+1, (byte) (14 + 1 * 16));
                        lvl.setBlock(destX, destY+2, (byte) (14 + 2 * 16));
                        return;
                    }
                }
            }
        }
    }

    private double calculateBlockChance(int[][] chunk, int continuous_blocks, int width, int height, int x, int y) {
        double block_chance = 0.0;
        if (continuous_blocks < platform_size) {
            block_chance = block_density + (continuous_blocks * 0.1);
        }
        else {
            block_chance = block_density - (continuous_blocks * 0.1);
        }
        //Is there a ledge (block->gap or gap->ledge) above us?
        boolean beneath_ledge = checkUnderLedge(chunk, width, height, x, y);
        if (beneath_ledge) {block_chance += 0.2;}
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
                if(checks[0] && Math.random() > 0.4){
                    lvl.setBlock(x+startX, y+startY-1, BlazeLevel.Tiles.COIN);
                }
            }else if(type == 'q'){
                lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.BLOCK_POWERUP);
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
                    lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.GROUND);
                }
                //middle single column
                else if (checks[2] && checks[3]) {
                    lvl.setBlock(x+startX, y+startY, BlazeLevel.Tiles.ROCK);
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
        boolean check = chunk[x][y] == 1;
        if (y == 0 || y > 20) {return true && check;}
        if (y > 0) {
            if(chunk[x][y-1] != 1) {return true && check;}
        }
        return false;
    }

    private boolean checkBlockFloat(int[][] chunk, int x, int y, int height) {
        if (y < height - jumpHeight) {
            if(chunk[x][y+1] != 1 && chunk[x][y+2] != 1 && chunk[x][y+3] != 1)  {return true;}
        }
        return false;
    }

    private boolean checkBlockLeft(int[][] chunk, int x, int y) {
        if(x == 0) {return true;}
        if(x > 0) {
            if(chunk[x-1][y] != 1) {return true;}
        }
        return false;
    }

    private boolean checkBlockRight(int[][] chunk, int x, int y, int width) {
        if(x < width - 1) {
            if(chunk[x+1][y] != 1){return true;}
        }
        else {return true;}
        return false;
    }
}
