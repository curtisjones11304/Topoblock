package org.geotools.tutorial.topoblock;

import java.awt.image.Raster;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;

public class Space extends SimpleApplication {

    private int gridSize;
    private Raster raster;
    private ExecutorService executorService; // Thread pool for multitasking

    Node blocks;

    Space(Raster raster, int gridSize) {
        this.raster = raster;
        this.gridSize = gridSize;
    }

    private int blockSize = 1;

    /*
     * public static void main(String[] args) { Space app = new Space(gridSize);
     * app.start(); }
     */

    @Override
    public void simpleInitApp() {
        // Set up the camera to be a floating camera
        viewPort.setClearFlags(true, true, true); // Clear depth, color, and alpha buffers
        flyCam.setMoveSpeed(100); // Adjust camera speed
        flyCam.setRotationSpeed(2); // Adjust camera rotation speed
        viewPort.setBackgroundColor(ColorRGBA.Cyan);
        // Create the world grid of blocks with variable heights

        // Initialize thread pool
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        createWorld();
    }

    private void createWorld() {
        Topoblock topo = new Topoblock();
        AssetManager assetManager = getAssetManager();
        Node rootNode = getRootNode();

        // Create materials upfront and reuse them
        Texture texture = assetManager.loadTexture("textures/coblestone.png");
        texture.setMinFilter(MinFilter.BilinearNoMipMaps); // Disable mipmaps
        texture.setMagFilter(MagFilter.Bilinear); // Use Bilinear for magnification

        Material blockMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blockMaterial.setTexture("ColorMap", texture);
        blockMaterial.getAdditionalRenderState().setDepthTest(true);
        blockMaterial.getAdditionalRenderState().setDepthWrite(true);

        int count = 0;

        // Create a task for each block generation
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {

                System.out.println("Loaded block " + (count) + "/" + (gridSize * gridSize));

                final int xCoord = x;
                final int yCoord = y;

                // Submit the block generation task to the executor
                executorService.submit(() -> {
                    int height = topo.getElevation(raster, xCoord, yCoord);

                    // Create the block geometry
                    Box box = new Box(blockSize, height / 2, blockSize);
                    Geometry block = new Geometry("Block_" + xCoord + "_" + yCoord, box);
                    block.setMaterial(blockMaterial);
                    block.setLocalTranslation(xCoord * blockSize, height / 2, yCoord * blockSize);

                    // Schedule this block to be added to the scene graph on the main thread
                    enqueue(() -> {
                        rootNode.attachChild(block);
                    });
                });
                count++;
            }
        }

        /*
         * for (int x = 0; x < (gridSize / 65); x++) { for (int y = 0; y < (gridSize /
         * 65); y++) {
         * 
         * System.out.println("Loaded block " + (count) + "/" + ((gridSize * gridSize) /
         * 130));
         * 
         * int height = topo.getElevation(raster, x, y);
         * 
         * // Create a node to hold all blocks for batch processing blocks = new
         * Node("Blocks");
         * 
         * Box box = new Box(blockSize, height / 2, blockSize); Geometry block = new
         * Geometry("Block_" + x + "_" + y, box); // Geometry block2 = new
         * Geometry("Block_" + (x + 0.01) + "_" + (y + 0.01), // box);
         * 
         * block.setMaterial(blockMaterial); // block2.setMaterial(wireframeMaterial);
         * 
         * // Set the position of the block in the world block.setLocalTranslation(x *
         * blockSize, height / 2, y * blockSize); // block2.setLocalTranslation(x *
         * blockSize, height / 2, y * blockSize);
         * 
         * // Attach the block to the root node rootNode.attachChild(block); // Attach
         * the block2 to the root node // rootNode.attachChild(block2);
         * 
         * count += 1; } }
         */
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Example of level of detail logic: skip rendering for blocks far from the
        // camera
        Vector3f cameraPosition = cam.getLocation();

        for (Spatial block : rootNode.getChildren()) {
            Vector3f blockPosition = block.getLocalTranslation();
            float distance = cameraPosition.distance(blockPosition);

            // Example: Don't render blocks farther than a certain distance
            if (distance > 100f) {
                block.setCullHint(Spatial.CullHint.Always); // Disable rendering
            } else {
                block.setCullHint(Spatial.CullHint.Never); // Enable rendering
            }
        }
    }

}
