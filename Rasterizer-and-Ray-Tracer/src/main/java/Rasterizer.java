package main.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

public class Rasterizer {
	
	private RenderObject object;
	
	private Camera camera;
	
	private ImageBuffer imageBuffer = new ImageBuffer();
	
	private ZBuffer zBuffer = new ZBuffer();
	
	public Rasterizer(RenderObject object, Camera camera) {
		this.object = object;
		this.camera = camera;
	}
	
	/*
	 * Initialise object to be rendered
	 * Position camera at object
	 * Polygon-by-polygon rendering:
	 * - Project vertices to 2D pixel points
	 * - Draw lines between vertices and construct the edge list
	 * - Fill polygon:
	 * - - Check Z buffer
	 * - - If closer update z buffer and paint pixel
	 */
	public void render() {
		try {
			object = new RenderObject();
			camera = new Camera(object.getPoints(), imageBuffer.getWidth());
			
			// TODO delete this
			System.out.println("CHECKING HERE");
			camera.quickFindMinMax(camera.projectToCameraCoords(object.getPoints()));
			System.out.println("CHECK DONE");

			//i.e. for each polygon
			for (int i=0; i<object.getFaces().length; i++) {
				// Pass polygon's vertices to project to 2D
				float[][] projectedVerts = projectToPixelCoords(object.getFaces()[i]);
				// Construct edge list
				SortedMap<Integer, LinkedList<float[]>> edgeList = 
						constructEdgeList(projectedVerts);
				// Fill the polygon
				fillPolygon(edgeList);
			}
			imageBuffer.convertToImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Complete");
	}

	/*
	 * For each scanline the polygon is filled between the first and last values
	 * of x. Z values are interpolated first which are used to check the Z-buffer.
	 * Once we know this polygon isn't behind another, we can update the Z-buffer
	 * and paint this pixel.
	 * 
	 * @params 
	 */
	private void fillPolygon(SortedMap<Integer, LinkedList<float[]>> edgeList) {
		
		for(int y : edgeList.keySet()) {
			// As we are rendering polygon-by-polygon we can just take the first and
			// last element in the linked list and fill between them.
			int startX = (int) edgeList.get(y).getFirst()[0];
			int endX = (int) edgeList.get(y).getLast()[0];
			
			float startZ = edgeList.get(y).getFirst()[1];
			float endZ = edgeList.get(y).getLast()[1];
			
			float z = startZ;
			float zInc;
			
			// To avoid divide by 0 case
			if(endX-startX-1 != 0) {
				zInc = (endZ-startZ)/(endX-startX-1);
			}
			else {
				zInc = (endZ-startZ);
			}
			
			for(int x=startX; x<=endX; x++) {
				if(x < imageBuffer.getWidth() && y < imageBuffer.getHeight() ) {
					if(zBuffer.check(x, y, z)) {
						// Paint pixel
						imageBuffer.paintPixel(x,y);
					}
				}
				z += zInc;
			}
		}
	}

	/*
	 * For each polygon (face) retrieve the vertices at each index. These are then projected
	 * to camera coordinates and converted to pixel coordinates.
	 * 
	 * @param vertexIntices the indices at which the vertices of this polygon are located
	 * @returns projectedVerts the pixel coordinates of each vertex in the polygon
	 */
	private float[][] projectToPixelCoords(int[] vertexIndices) {
		float[][] polygonVerts = new float[3][3];
		float[][] projectedVerts = new float[3][3];

		polygonVerts[0] = object.getPoints()[vertexIndices[0]];
		polygonVerts[1] = object.getPoints()[vertexIndices[1]];
		polygonVerts[2] = object.getPoints()[vertexIndices[2]];

		float[][] cameraCoords = camera.projectToCameraCoords(polygonVerts);

		for (int j=0; j<3; j++) {
			projectedVerts[j][0] = (int) Math.ceil(cameraCoords[j][0]);
			projectedVerts[j][1] = (int) Math.ceil(cameraCoords[j][1]);
			// For use by Z-buffer
			projectedVerts[j][2] = cameraCoords[j][2];
		}
		
		return projectedVerts;

	}
	
	/*
	 * Finds the pixel points of the edges of the polygon and then constructs the edge list. 
	 * 
	 * @param polygonPixels Polygon vertices (in pixels)
	 * @returns edgeList Points where an edge has intersected a scanline with corresponding z values
	 */
	private SortedMap<Integer, LinkedList<float[]>> constructEdgeList(float[][] polygonPixels) {
		SortedMap<Integer, LinkedList<float[]>> edgeList = 
				new TreeMap<Integer, LinkedList<float[]>>();
		
		float[][] line1, line2, line3;
		
		// V0 to V1
		line1 = bresenham((int)polygonPixels[0][0],(int)polygonPixels[0][1], polygonPixels[0][2],
				(int)polygonPixels[1][0], (int)polygonPixels[1][1], polygonPixels[1][2]);
		// V1 to V2
		line2 = bresenham((int)polygonPixels[1][0],(int)polygonPixels[1][1], polygonPixels[1][2],
				(int)polygonPixels[2][0], (int)polygonPixels[2][1], polygonPixels[2][2]);
		// V0 to V2
		line3 = bresenham((int)polygonPixels[0][0],(int)polygonPixels[0][1], polygonPixels[0][2],
				(int)polygonPixels[2][0], (int)polygonPixels[2][1], polygonPixels[2][2]);
		
		float[][][] lines = {line1, line2, line3};
		
		for(int i=0; i<lines.length; i++) {
			//i.e for each point in line
			for(int j=0; j<lines[i].length; j++) {
				
				if(edgeList.containsKey((int) lines[i][j][1])) {
					float[] xz = {lines[i][j][0], lines[i][j][2]};
					// Sort the floats[] by values of x
					if(xz[0] < edgeList.get((int) lines[i][j][1]).getFirst()[0]) {
						edgeList.get((int) lines[i][j][1]).add(0, xz);
					}
				}
				else {
					LinkedList<float[]> values = new LinkedList<float[]>();
					float[] xz = {lines[i][j][0], lines[i][j][2]};
					values.add(xz);
					edgeList.put((int) lines[i][j][1], values);
				}
			}
		}
		
		return edgeList;
	}
	
	/*
	 * Bresenham's line algorithm on the x and y values with interpolation of the Z values along 
	 * the line.
	 * 
	 * @param startX x1
	 * @param startY y1
	 * @param startZ z1
	 * @param endX x2
	 * @param endY y2
	 * @param endZ z2
	 * @returns line The coordinates of the edge connecting two vertices with their corresponding z.
	 */
	private float[][] bresenham(int startX, int startY, float startZ, int endX, int endY, float endZ) {
		float[][] line = null;
		
		if (startX > endX) {
			line = bresenham(endX, endY, endZ, startX, startY, startZ);
			return line;
		}
		else {
			double gradient = 0;
			
			if((endY-startY) != 0) {
				gradient = (double)(endY-startY)/(double)(endX-startX);
			}
			
			if (gradient < 0) {
				line = bresenham(startX, -startY, startZ, endX, -endY, endZ);
				for(int i=0; i<line.length; i++) {
					line[i][1] *= -1;
				}
				return line;
			}
			else if (gradient > 1) {
				line = bresenham(startY, startX, startZ, endY, endX, endZ);
				for(int i=0; i<line.length; i++) {
					float temp = line[i][0];
					line[i][0] = line[i][1];
					line[i][1] = temp;
				}
				return line;
			}
			else {
				ArrayList<ArrayList<Integer>> points = new ArrayList<ArrayList<Integer>>();
				int x = startX;
				int y = startY;
				int dx = endX-startX;
				int dy = endY-startY;
				int d = (2*dy)-dx;
				while (x <= endX) {
					ArrayList<Integer> point = new ArrayList<Integer>();
					point.add(x);
					point.add(y);
					points.add(point);
					x++;
					if (d < 0) {
						d = d + (2*dy);
					}
					else {
						d = d + (2*(dy-dx));
						y++;
					}
				}
				
				line = new float[points.size()][3];
				
				for(int i=0; i<points.size(); i++) {
					line[i][0] = points.get(i).get(0);
					line[i][1] = points.get(i).get(1);
				}
				
				// Interpolate Z values along line
				line[0][2] = startZ;
				float zInc = (endZ-startZ)/((float)line.length-1);
				
				for(int i=1; i<line.length; i++) {
					line[i][2] = line[i-1][2] + zInc;
				}
				
				return line;
			}
		}
	}
}
