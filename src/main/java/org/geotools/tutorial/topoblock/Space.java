/*
 * Space
 * 
 * By Curtis Jones, Jon Ayuco, Nikhil Chaba, Andrew Bastien
 * 
 * The purpose of this Class is to simulate an algorithm that would transfer the topographic data from
 * a geoTIFF file into the videogame Minecraft. This class builds the enviornment and blocks using the JmonkeyEngine.
 * 
 * Dependencies:
 *  GeoTools:
 *      Process geoTIFF files by extracting the raster layers from the images. These layers contain topographic 
 *      height data 
 *  JMonkeyEngine: 
 *      A graphical game engine that can simulate how this functionality would work in minecraft.

 */

package org.geotools.tutorial.topoblock;

import java.awt.image.Raster;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;

/* 
 * Functions:
 * 
 *  simpleInitApp()): 
 *      initializes the environment
 *      initializes camera and camera properties
 *  createWorld()
 *      creates environment
 *      sets properties for geometry used to represent minecraft blocks
 *      iterates through raster coordinates to find heights for each block
 *      spawns each individual block
 */

public class Space extends SimpleApplication {

    // gets rastar and grid size from Topoblock
    private int gridSize;
    private Raster raster;
    private ExecutorService executorService; // Thread pool for multitasking

    // constructor
    Space(Raster raster, int gridSize) {
        this.raster = raster;
        this.gridSize = gridSize;
    }

    // sets size of individual blocks to 1 to keep uniformity
    private int blockSize = 1;

    // function to initialize world and camera
    @Override
    public void simpleInitApp() {
        // sets up camera
        viewPort.setClearFlags(true, true, true);
        flyCam.setMoveSpeed(100);
        flyCam.setRotationSpeed(2);
        viewPort.setBackgroundColor(ColorRGBA.Cyan);

        // initialize thread pool
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // initialize world
        createWorld();
    }

    /*
     * function to: set properties for each block iterate through the raster grid,
     * coordinates and extract height data use height and coordinate data to spawn
     * blocks
     */
    private void createWorld() {
        AssetManager assetManager = getAssetManager();
        Node rootNode = getRootNode();

        // creates texture to use for all blocks
        Texture texture = assetManager.loadTexture("textures/coblestone.png");
        texture.setMinFilter(MinFilter.BilinearNoMipMaps); // Disable mipmaps
        texture.setMagFilter(MagFilter.Bilinear); // Use Bilinear for magnification

        // sets material and texture for all blocks
        Material blockMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blockMaterial.setTexture("ColorMap", texture);
        blockMaterial.getAdditionalRenderState().setDepthTest(true);
        blockMaterial.getAdditionalRenderState().setDepthWrite(true);

        // double value to display loading progress
        double loadingPercentage = 0;

        // nested for loop do traverse over (x, y) coordinates in raster
        for (int x = 0; x < (gridSize); x++) {
            for (int y = 0; y < (gridSize); y++) {

                // displays what percentage of the map is loaded every 100000 blocks
                if (loadingPercentage % 1000000 == 0)
                    System.out.println("Loaded block " + (loadingPercentage / (gridSize * gridSize) * 100) + "%");

                // creates final ints for certain calls
                final int X = x;
                final int Y = y;

                /*
                 * the following is an implementation of multithreading to quickly load the
                 * environment
                 */

                // submits the generation task to the executor
                executorService.submit(() -> {

                    // extracts height from the given (x, y) coordinate
                    int height = Topoblock.getElevation(raster, X, Y);

                    // creates the block geometry
                    Box box = new Box(blockSize, blockSize, blockSize);
                    Geometry block = new Geometry("Block_" + X + "_" + Y, box);
                    block.setMaterial(blockMaterial);

                    // sets block location
                    block.setLocalTranslation(X * blockSize, height / 2, Y * blockSize);

                    // queues this block to be added to the environment on the main thread
                    enqueue(() -> {
                        rootNode.attachChild(block);
                    });
                });
                loadingPercentage++;
            }
        }
    }

    // sets logic (currently unutilized)
    @Override
    public void simpleUpdate(float tpf) {
    }
}
