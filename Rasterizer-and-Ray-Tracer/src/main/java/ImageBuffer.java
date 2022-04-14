package main.java;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageBuffer extends Buffer {
	
	private int backgroundColour = Color.BLACK.getRGB();
	
	private BufferedImage bufferedImage;
	
	/*
	 * Initialises the buffered image of size height x width and sets the background
	 * to be black
	 */
	public ImageBuffer() {
		bufferedImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		
		setBackgroundColour(backgroundColour);
	}
	
	/*
	 * Sets the value of all pixels in the buffer.
	 * 
	 * @param bg Given background colour
	 */
	private void setBackgroundColour(int bg) {
		this.backgroundColour = bg;
		
		for(int y=0; y<this.getHeight(); y++) {
			for(int x=0; x<this.getWidth(); x++) {
				bufferedImage.setRGB(x, y, backgroundColour);
			}
		}
	}

	/*
	 * Updates the value in the image buffer of the point x,y
	 * 
	 * @param x 
	 * @param y
	 * @param colour RGB values
	 */
	public void paintPixel(int x, int y, float[] colour) {
		// Deal with rounding errors in float type
		for (int i=0; i<3; i++) {
			if(colour[i] < 0) {
				colour[i] = 0;
			}
			
			if(colour[i] > 1) {
				colour[i] = 1;
			}
		}
		
		int colourRGB = convertToRGB(colour);
		bufferedImage.setRGB(x, y, colourRGB);
	}
	
	/*
	 * Converts the image buffer into an image
	 * 
	 * @param name Name of file
	 */
	public void convertToImage(String name) throws IOException {
		File outputfile = new File(name);
		ImageIO.write(bufferedImage, "jpg", outputfile);
	}
	
	public int convertToRGB(float[] colour) {
		Color colourRGB = new Color(colour[0],colour[1],colour[2]);
		return colourRGB.getRGB();
	}
}
