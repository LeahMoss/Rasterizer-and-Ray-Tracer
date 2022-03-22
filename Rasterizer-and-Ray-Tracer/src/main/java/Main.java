package main.java;

import java.io.IOException;

public class Main {

	/*
	 * Initialise object to be rendered
	 * Position camera at object
	 * Set background colour of image buffer
	 * Initialise Z-buffer to infinity
	 * 
	 * For each polygon
	 * 		Project to 2D
	 * 		Find corresponding pixel values
	 * 		For each pixel
	 * 			If closer than value in Z-buffer
	 * 				Update Z-buffer
	 * 				Paint pixel
	 */
	public static void main(String[] args) {
		ImageBuffer image = new ImageBuffer();
		ZBuffer zBuffer = new ZBuffer();
		RenderObject object;
		Camera camera;
		
		try {
			object = new RenderObject();
			camera = new Camera(object.getPoints(), image.getWidth(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Complete");
	}

}
