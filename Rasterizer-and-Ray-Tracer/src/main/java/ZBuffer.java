package main.java;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ZBuffer extends Buffer {
	
	private float[][] buffer;

	/*
	 * Initialises a buffer of size height x width with values of infinity
	 */
	public ZBuffer() {
		buffer = new float[this.height][this.width];
		
		for(int y=0; y<this.getHeight(); y++) {
			for(int x=0; x<this.getWidth(); x++) {
				buffer[y][x] = Float.POSITIVE_INFINITY;
			}
		}
	}
	
	/*
	 * Checks the Z-buffer to see if the given z for point x,y is closer than the one
	 * currently in the Z-buffer. If it is closer it updates this value.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @returns boolean True if Z-buffer has been updated
	 */
	public boolean check(int x, int y, float z) {
		if(z<buffer[y][x]) {
			buffer[y][x] = z;
			
			return true;
		}
		
		return false;
	}
	
	/*
	 * Converts the z buffer to an image
	 * 
	 * @param name Name of file
	 */
	public void convertToImage(String name) throws IOException {
		BufferedImage zBuffer= new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_GRAY);
		
		// Find the min and max z value in mesh
		// Use min as the near clipping plane
		// Use max as the far clipping plane
		float max = 0, min = Float.POSITIVE_INFINITY;
		for(int y=0; y<this.height; y++) {
			for(int x=0; x<this.width; x++) {
				if (max < buffer[y][x] && buffer[y][x] != Float.POSITIVE_INFINITY) {
					max = buffer[y][x];
				}
				
				if (min > buffer[y][x]) {
					min = buffer[y][x];
				}
			}
		}
		
		byte[] databuffer = ((DataBufferByte)zBuffer.getRaster().getDataBuffer()).getData();
		
		for(int y=0; y<this.height; y++) {
			for(int x=0; x<this.width; x++) {
				if (buffer[y][x] == Float.POSITIVE_INFINITY) {
					databuffer[(y*width)+x] = (byte) 0;
				}
				else {
					// Z value divided by max value to get val between 0 and 1
					// Invert so when val = 1 it becomes 0 (want far away to be black = 0)
					databuffer[(y*width)+x] = (byte) (((((buffer[y][x]-min)/(max-min))-1f)*-1f)*255f);
				}
			}
		}
		
		File outputfile = new File(name);
		ImageIO.write(zBuffer, "jpg", outputfile);
	}
	
}
