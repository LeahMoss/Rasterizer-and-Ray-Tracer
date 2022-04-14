package main.java;

public class Main {

	public static void main(String[] args) {
		RenderObject object = null;
		Camera camera = null;
		Rasterizer rasterizer = new Rasterizer(object, camera);
		rasterizer.start();
	}

}
