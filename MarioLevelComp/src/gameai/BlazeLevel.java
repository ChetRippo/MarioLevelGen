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
    static final int chunkWidth = 8;
    static final int floor = (int)Math.floor(Math.random()*10) + 7;

    public BlazeLevel(int width, int height, long seed, int difficulty,
                      int type) {
        super(width, height);
        create(seed, difficulty, type);
    }

    private void create(long seed, int difficulty, int type) {

        double block_density = 0.4; //Between 0 - 1
        ChunkBuilder c = new ChunkBuilder(this, block_density);

        buildStartChunk(c);

        buildChunk(c, chunkWidth);

    }

    private void buildChunk(ChunkBuilder c, int startX) {
        c.buildChunks(startX, 7, chunkWidth, chunkWidth, HILL_TOP);

        int isLastChunk = (int)Math.floor(Math.random()*startX);
        boolean last = (isLastChunk > 60);
        if(last){
            buildFinalChunk(c, startX + chunkWidth);
        }else{
            buildChunk(c, startX+chunkWidth);
        }
    }

    private void buildStartChunk(ChunkBuilder c){
        c.buildChunks(0, floor-3, chunkWidth, chunkWidth, HILL_TOP);
    }

    private void buildFinalChunk(ChunkBuilder c, int startX){
        xExit = startX+4;
        yExit = floor-3;
        c.buildChunks(startX, floor-3, chunkWidth, chunkWidth, HILL_TOP);
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
