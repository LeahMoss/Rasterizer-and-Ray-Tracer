package main.java;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageBuffer extends Buffer {
	
	private int backgroundColour = Color.WHITE.getRGB();
	
	private BufferedImage bufferedImage;
	
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
	 */
	public void paintPixel(int x, int y, float[] colour) {
		int colourRGB = convertToRGB(colour);
		bufferedImage.setRGB(x, y, colourRGB);
	}
	
	/*
	 * Converts the image buffer into an image
	 */
	public void convertToImage() throws IOException {
		File outputfile = new File("image.jpg");
		ImageIO.write(bufferedImage, "jpg", outputfile);
		
		
	}
	
	public int convertToRGB(float[] colour) {
		Color colourRGB = new Color(colour[0],colour[1],colour[2]);
		return colourRGB.getRGB();
	}
}
