package main.java;

import java.io.File;
import java.io.IOException;

import org.smurn.jply.Element;
import org.smurn.jply.ElementReader;
import org.smurn.jply.PlyReader;
import org.smurn.jply.PlyReaderFile;

public class RenderObject {

    /* data fields to store points, colors, faces information read from PLY file */
	private float[][] points = null;
	private float[][] colours = null;
	private int[][] faces = null;
	
	private String path = getClass().getClassLoader().getResource("teapot.ply")
							.toString().substring(6);
	private File file = new File(path);
	
	public RenderObject() throws IOException {
		
		PlyReader ply = new PlyReaderFile(file);
	
		int vertexCount = ply.getElementCount("vertex");
		int triangleCount = ply.getElementCount("face");
		
		ElementReader reader;
	
		/* Iterate to read elements */
		while ((reader = ply.nextElementReader()) != null) {
			String elementType = reader.getElementType().getName();
	
			if (elementType.equals("vertex")) {
				if (points != null)
					continue;
				points = new float[vertexCount][3];
	
				Element element;
				int x = 0;
				while ((element = reader.readElement()) != null) {
	                /* manipulated array indexes to store  */
					points[x][0] = (float) element.getDouble("x");
					points[x][1] = (float) element.getDouble("y");
					points[x][2] = (float) element.getDouble("z");
					x++;
				}
	
			} else if (elementType.equals("face")) {
				
				if (faces != null)
					continue;
				faces = new int[triangleCount][3];
				colours = new float[triangleCount][3];
	
				Element element;
				int x = 0;
				while ((element = reader.readElement()) != null) {
					int[] vertex_index = null;
	
					try {
						vertex_index = element.getIntList("vertex_indices");
					}
					
					catch (Exception e) {}
	
					if(vertex_index == null) {
						try {
							vertex_index = element.getIntList("vertex_index");
						}
						
						catch (Exception e) {}
					}
	
					if(vertex_index == null) {
						throw new IOException("Failed to read vertices");
					}
	
					faces[x] = vertex_index;
					colours[x][0] = (float) element.getDouble("red") / 255f;
					colours[x][1] = (float) element.getDouble("green") / 255f;
					colours[x][2] = (float) element.getDouble("blue") / 255f;
					
					x++;
				}
			}
	
			reader.close();
		}
	
		ply.close();
	}
	   
	    /* additional methods to return detailed description of object for further manipulation */
	public float[][] getPoints(){
		return this.points;
	}
	
	public int[][] getFaces(){
		return this.faces;
	}
	
	public float[][] getColors(){
		return this.colours;
	}
	
}
