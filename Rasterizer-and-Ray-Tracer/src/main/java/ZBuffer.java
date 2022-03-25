package main.java;

public class ZBuffer extends Buffer {
	
	private float[][] buffer;

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
	
}
