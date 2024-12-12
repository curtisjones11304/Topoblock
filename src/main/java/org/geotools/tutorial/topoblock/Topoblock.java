/*
 * Topoblock
 * 
 * By Curtis Jones, Jon Ayuco, Nikhil Chaba, Andrew Bastien
 * 
 * The purpose of this Class is to simulate an algorithm that would transfer the topographic data from
 * a geoTIFF file into the videogame Minecraft. We achieve this by using JME3, a graphical engine made 
 * for Java, to simulate the blocky landscape of Minecraft. 
 * 
 * Dependencies:
 *  GeoTools:
 *      Process geoTIFF files by extracting the raster layers from the images. These layers contain topographic 
 *      height data 
 *  JMonkeyEngine: 
 *      A graphical game engine that can simulate how this functionality would work in minecraft.

 */

package org.geotools.tutorial.topoblock;

import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;

/* 
 * Functions:
 * 
 *  render(File file): 
 *      Reads the geoTIFF file and converts it into a raster layer
 *      Extracts data from the image into an array
 *      Starts the graphical program
 *  getElevation(Raster raster, int x, int y):
 *      Uses the indices of the image to extract the topographic height at the point x, y
 *  scalar(float value):
 *      Scales the height of the resultant topographic data to conform with minecraft's
 *      height restrictions
 */

public class Topoblock {

    // stores the height and width of image
    private static int IMAGE_WIDTH;
    private static int IMAGE_HEIGHT;

    // stores the minimum and maximum toporaphic heights in image
    private static float MAX_VAL;
    private static float MIN_VAL;

    // stores extracted data from image
    private static float[] DATA;

    public static void main(String[] args) {

        // Insert the file path for desired geoTIFF file
        File file = new File("USGS_1_n42w074_20241010.tif");
        render(file);
    }

    // function to manipulate data and start the graphical engine
    public static void render(File file) {
        try {
            // extracts raster layer from the geoTIFF file
            GeoTiffReader reader = new GeoTiffReader(file);
            GridCoverage2D coverage = reader.read(null);
            Raster raster = coverage.getRenderedImage().getData();

            // sets dimensions from raster image
            IMAGE_HEIGHT = raster.getHeight();
            IMAGE_WIDTH = raster.getWidth();

            // extracts the data from the raster image into an array
            DATA = ((DataBufferFloat) raster.getDataBuffer()).getData();

            // initializes space object and starts graphical engine.
            // *Maintains an equal number of pixels across both dimensions when rendering
            // blocks due to constrictions of Space*
            Space space = new Space(raster, Math.min(IMAGE_WIDTH, IMAGE_HEIGHT));
            space.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // extracts elevation at x, y from DATA
    public static int getElevation(Raster raster, int x, int y) {
        // indices in array where you can find x, y
        int index = (y * IMAGE_WIDTH) + x;
        float value = DATA[index];
        // sends value to be scaled
        int elevation = scalar(value);
        return elevation;
    }

    // scales elevation value in tune with the restrictions of minecraft
    public static int scalar(float value) {

        // finds the maximum and minimum elevation across DATA
        for (float val : DATA) {
            if (val < MIN_VAL)
                MIN_VAL = val;
            if (value > MAX_VAL)
                MAX_VAL = val;
        }

        // maximum and minimum minecraft values
        float minMinecraft = 0;
        float maxMinecraft = 320;

        // scaling equation
        float scaledValue = minMinecraft + ((value - MIN_VAL) / (MAX_VAL - MIN_VAL)) * (maxMinecraft - minMinecraft);
        int roundedValue = Math.round(scaledValue);

        return roundedValue;
    }
}