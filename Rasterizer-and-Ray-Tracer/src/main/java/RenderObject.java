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

	private String path = getClass().getClassLoader().getResource("blub.ply")
			.toString().substring(6);
	private File file = new File(path);

	/*
	 * Reads the ply file and stores the information in this object.
	 * 
	 * @throws IOException
	 */
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
				colours = new float[vertexCount][3];

				Element element;
				int x = 0;
				while ((element = reader.readElement()) != null) {
					/* manipulated array indexes to store  */
					points[x][0] = (float) element.getDouble("x");
					points[x][1] = (float) element.getDouble("y");
					points[x][2] = (float) element.getDouble("z");

					colours[x][0] = (float) element.getDouble("red") / 255f;
					colours[x][1] = (float) element.getDouble("green") / 255f;
					colours[x][2] = (float) element.getDouble("blue") / 255f;
					
					x++;
				}

			} else if (elementType.equals("face")) {

				if (faces != null)
					continue;
				faces = new int[triangleCount][3];

				Element element;
				int x = 0;
				while ((element = reader.readElement()) != null) {
					int[] vertex_indices = null;

					try {
						vertex_indices = element.getIntList("vertex_indices");
					}

					catch (Exception e) {}

					if(vertex_indices == null) {
						try {
							vertex_indices = element.getIntList("vertex_index");
						}

						catch (Exception e) {}
					}

					if(vertex_indices == null) {
						throw new IOException("Failed to read vertices");
					}

					faces[x] = vertex_indices;


					x++;
				}
			}

			reader.close();
		}

		ply.close();
	}

	/*
	 * @return points
	 */
	public float[][] getPoints(){
		return this.points;
	}

	/*
	 * @return faces
	 */
	public int[][] getFaces(){
		return this.faces;
	}

	/*
	 * @return colours
	 */
	public float[][] getColors(){
		return this.colours;
	}

}
