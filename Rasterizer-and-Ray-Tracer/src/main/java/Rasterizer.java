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
	
	private ImageBuffer image = new ImageBuffer();
	
	private ZBuffer zBuffer = new ZBuffer();
	
	public Rasterizer(RenderObject object, Camera camera) {
		this.object = object;
		this.camera = camera;
	}
	
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
	public void render() {

		try {
			object = new RenderObject();
			camera = new Camera(object.getPoints(), image.getWidth(), false);
			//int[][][] pixelsByPolygon = projectToPixelCoords();
			
			//i.e. for each polygon
			for (int i=0; i<object.getFaces().length; i++) {
				// Pass polygon's vertices to project to 2D
				int[][] projectedVerts = projectToPixelCoords(object.getFaces()[i]);
				SortedMap<Integer, LinkedList<Integer>> edgeList = 
						constructEdgeList(projectedVerts);
				fillPolygon(edgeList);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Complete");
	}

	private void fillPolygon(SortedMap<Integer, LinkedList<Integer>> edgeList) {
		
	}

	/*
	 * For each polygon (face) retrieve the vertices at each index.
	 * Project the vertices to camera coordinates.
	 */
	private int[][] projectToPixelCoords(int[] vertexIndices) {
		float[][] polygonVerts = new float[3][3];
		int[][] projectedVerts = new int[3][2];

		polygonVerts[0] = object.getPoints()[vertexIndices[0]];
		polygonVerts[1] = object.getPoints()[vertexIndices[1]];
		polygonVerts[2] = object.getPoints()[vertexIndices[2]];

		float[][] cameraCoords = camera.projectToCameraCoords(polygonVerts);

		for (int j=0; j<3; j++) {
			projectedVerts[j][0] = (int) Math.ceil(cameraCoords[j][0]+camera.getCx());
			projectedVerts[j][1] = (int) Math.ceil(cameraCoords[j][1]+camera.getCy());
		}
		
		return projectedVerts;

	}
	
	/*
	 * Constructs an edge list for the given polygon vertices.
	 */
	private SortedMap<Integer, LinkedList<Integer>> constructEdgeList(int[][] polygonPixels) {
		SortedMap<Integer, LinkedList<Integer>> edgeList = 
				new TreeMap<Integer, LinkedList<Integer>>();
		
		int[][] line1, line2, line3;
		
		// V0 to V1
		line1 = bresenham(polygonPixels[0][0],polygonPixels[0][1], 
				polygonPixels[1][0], polygonPixels[1][1]);
		// V1 to V2
		line2 = bresenham(polygonPixels[1][0],polygonPixels[1][1], 
				polygonPixels[2][0], polygonPixels[2][1]);
		// V0 to V2
		line3 = bresenham(polygonPixels[0][0],polygonPixels[0][1], 
				polygonPixels[2][0], polygonPixels[2][1]);
		
		int[][][] lines = {line1, line2, line3};
		
		for(int i=0; i<lines.length; i++) {
		//i.e for each point in line
			for(int j=0; j<lines[i].length; j++) {
				if(edgeList.containsKey(lines[i][j][1])) {
					edgeList.get(lines[i][j][1]).add(lines[i][j][0]);
					edgeList.get(lines[i][j][1]).sort(Comparator.naturalOrder());
				}
				else {
					LinkedList<Integer> values = new LinkedList<Integer>();
					values.add(lines[i][j][0]);
					edgeList.put(lines[i][j][1], values);
				}
			}
		}
		
		return edgeList;
	}
	
	/*
	 * Bresenham's line algorithm
	 */
	private int[][] bresenham(int startX, int startY, int endX, int endY) {
		int[][] line = null;
		
		if (startX > endX) {
			line = bresenham(endX, endY, startX, startY);
			return line;
		}
		else {
			double gradient = 0;
			
			if((endY-startY) != 0) {
				gradient = (double)(endY-startY)/(double)(endX-startX);
			}
			
			if (gradient < 0) {
				line = bresenham(startX, -startY, endX, -endY);
				for(int i=0; i<line.length; i++) {
					line[i][1] *= -1;
				}
				return line;
			}
			else if (gradient > 1) {
				line = bresenham(startY, startX, endY, endX);
				for(int i=0; i<line.length; i++) {
					int temp = line[i][0];
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
				
				line = new int[points.size()][2];
				
				for(int i=0; i<points.size(); i++) {
					line[i][0] = points.get(i).get(0);
					line[i][1] = points.get(i).get(1);
				}
				
				return line;
			}
		}
	}
}
