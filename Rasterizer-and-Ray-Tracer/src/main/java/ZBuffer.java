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
	
}
