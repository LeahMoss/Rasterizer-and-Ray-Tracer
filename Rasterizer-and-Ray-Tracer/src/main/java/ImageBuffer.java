package main.java;

public class ImageBuffer extends Buffer {
	
	private float[][][] buffer;
	
	private float[] backgroundColour = {0,0,0};
	
	public ImageBuffer() {
		buffer = new float[this.height][this.width][3];
		
		setBackgroundColour(backgroundColour);
	}
	
	private void setBackgroundColour(float[] bg) {
		this.backgroundColour = bg;
		
		for(int y=0; y<this.getHeight(); y++) {
			for(int x=0; x<this.getWidth(); x++) {
				buffer[y][x] = this.backgroundColour;
			}
		}
	}

}
