package main.java;

import java.io.IOException;
import java.util.ArrayList;
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
	 * of x. Z and RGB values are interpolated first. Then we check the Z-buffer.
	 * Once we know this polygon isn't behind another, we can update the Z-buffer
	 * and paint this pixel.
	 * 
	 * @params 
	 */
	private void fillPolygon(SortedMap<Integer, LinkedList<float[]>> edgeList) {
		// For a given value of y, we can take the first and last element in the edge list
		// and interpolate the z and RGB values between them. To do this we must find
		// the increment value for each value of x between these 2 points.
		for(int y : edgeList.keySet()) {			
			int 	startX = (int) edgeList.get(y).getFirst()[0],
					endX = (int) edgeList.get(y).getLast()[0];
			
			float 	z = edgeList.get(y).getFirst()[1],
					r = edgeList.get(y).getFirst()[2], 
					g= edgeList.get(y).getFirst()[3], 
					b = edgeList.get(y).getFirst()[4];
			
			float 	zInc = findIncrement(z, edgeList.get(y).getLast()[1], endX-startX), 
					rInc = findIncrement(r, edgeList.get(y).getLast()[2], endX-startX), 
					gInc = findIncrement(g, edgeList.get(y).getLast()[3], endX-startX), 
					bInc = findIncrement(b, edgeList.get(y).getLast()[4], endX-startX);
			
			for(int x=startX; x<=endX; x++) {
				if(x < imageBuffer.getWidth() && x > 0 && y < imageBuffer.getHeight() && y > 0) {
					if(zBuffer.check(x, y, z)) {
						// Paint pixel
						float[] colour = {r, g, b};
						imageBuffer.paintPixel(x,y,colour);
					}
				}
				if (x == endX-1) {
					z = edgeList.get(y).getLast()[1];
					r = edgeList.get(y).getLast()[2];
					g = edgeList.get(y).getLast()[3];
					b = edgeList.get(y).getLast()[4];
				}
				else {
					z += zInc;
					r += rInc;
					g += gInc;
					b += bInc;
				}
				
			}
		}
	}
	
	private float findIncrement(float first, float last, float number) {
		float inc;
		// To avoid divide by 0 case
		if(number-1 != 0) {
			// Don't want to have this number be less than 0.000000
			inc = (last-first)/(number-1f);
		}
		else {
			inc = last-first;
		}
		return inc;
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
		float[][] projectedVerts = new float[3][6];

		polygonVerts[0] = object.getPoints()[vertexIndices[0]];
		polygonVerts[1] = object.getPoints()[vertexIndices[1]];
		polygonVerts[2] = object.getPoints()[vertexIndices[2]];

		float[][] cameraCoords = camera.projectToCameraCoords(polygonVerts);

		for (int j=0; j<3; j++) {
			projectedVerts[j][0] = (int) Math.ceil(cameraCoords[j][0]);
			projectedVerts[j][1] = (int) Math.ceil(cameraCoords[j][1]);
			// For use by Z-buffer
			projectedVerts[j][2] = cameraCoords[j][2];
			projectedVerts[j][3]  = object.getColors()[vertexIndices[j]][0];
			projectedVerts[j][4]  = object.getColors()[vertexIndices[j]][1];
			projectedVerts[j][5]  = object.getColors()[vertexIndices[j]][2];
		}
		
		return projectedVerts;

	}
	
	/*
	 * Finds the pixel points of the edges of the polygon and then constructs the edge list. 
	 * The edge list has a linked list for each y value. The linked list contains an array
	 * for every pixel where a line has intersected the scan line. The array contains the x
	 * value, z value and the RGB data for where that line has intersected.
	 * 
	 * @param polygonPixels Polygon vertices (in pixels)
	 * @returns edgeList Points where an edge has intersected a scanline with corresponding z values
	 */
	private SortedMap<Integer, LinkedList<float[]>> constructEdgeList(float[][] polygonPixels) {
		SortedMap<Integer, LinkedList<float[]>> edgeList = 
				new TreeMap<Integer, LinkedList<float[]>>();
		
		float[][] line1, line2, line3;
		// V0 to V1
		line1 = bresenham((int)polygonPixels[0][0],(int)polygonPixels[0][1],
				(int)polygonPixels[1][0], (int)polygonPixels[1][1]);
		// V1 to V2
		line2 = bresenham((int)polygonPixels[1][0],(int)polygonPixels[1][1], 
				(int)polygonPixels[2][0], (int)polygonPixels[2][1]);
		// V0 to V2
		line3 = bresenham((int)polygonPixels[0][0],(int)polygonPixels[0][1],
				(int)polygonPixels[2][0], (int)polygonPixels[2][1]);
		
		float[][][] lines = {line1, line2, line3};

		for(int i=0; i<lines.length; i++) {
			// Before constructing edge list, interpolate Z and RGB values
			lines[i] = interpolateLine(lines[i], polygonPixels);
			
			//i.e for each point in line
			for(int j=0; j<lines[i].length; j++) {
				int y = (int) lines[i][j][1];
				float[] xzRGB = {lines[i][j][0], lines[i][j][2], 
						lines[i][j][3], lines[i][j][4], lines[i][j][5]};
				
				if(edgeList.containsKey(y)) {
					// Insert xzRGB to the correct place in the linked list
					boolean set = false;
					
					for(int k=0; k<edgeList.get(y).size(); k++) {
						float[] current = edgeList.get(y).get(k);
						
						if(xzRGB[0] == current[0]) {
							// Already in edge list
							set = true;
							break;
						}
						
						// Insert before current value being checked
						if(xzRGB[0] < current[0]) {
							edgeList.get(y).add(k, xzRGB);
							set = true;
							break;
						}
					}
					
					// xzRGB's x value is greater than all items already in the list
					if(!set) {
						edgeList.get(y).addLast(xzRGB);
					}
					
				}
				else {
					LinkedList<float[]> values = new LinkedList<float[]>();
					values.add(xzRGB);
					edgeList.put(y, values);
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
	private float[][] bresenham(int startX, int startY, int endX, int endY) {
		
		float[][] line = null;
		
		// Make sure startX < endX
		if (startX > endX) {
			line = bresenham(endX, endY, startX, startY);
			return line;
		}
		else {
			double gradient = 0;
			if((endY-startY) != 0) {
				gradient = (double)(endY-startY)/(double)(endX-startX);
			}
			
			// Negative gradient
			if (gradient < 0) {
				// Flip y signs and rerun
				line = bresenham(startX, -startY, endX, -endY);
				// Return y signs back to normal
				for(int i=0; i<line.length; i++) {
					line[i][1] *= -1;
				}
				return line;
			}
			// Gradient > 1
			else if (gradient > 1) {
				// Swap X and Y
				line = bresenham(startY, startX, endY, endX);
				// Swap X and Y back
				for(int i=0; i<line.length; i++) {
					float temp = line[i][0];
					line[i][0] = line[i][1];
					line[i][1] = temp;
				}
				return line;
			}
			// 0 <= Gradient <= 1
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
				
				line = new float[points.size()][6];
				
				for(int i=0; i<points.size(); i++) {
					line[i][0] = points.get(i).get(0);
					line[i][1] = points.get(i).get(1);
				}
				
				return line;
			}
		}
	}
	
	/*
	 * Interpolation of Z and RBG values along a line
	 * 
	 * @param line a line with z and RGB values assigned to the first and last index
	 */
	public float[][] interpolateLine(float[][] line, float[][]polygonPixels) {
		float endX = line[line.length-1][0];
		float endY = line[line.length-1][1];
		
		// Find the correct Z and RGB values to assign to the start and end of the line
		for(int i=0; i<3; i++) {
			
			if (polygonPixels[i][0] == line[0][0] && polygonPixels[i][1] == line[0][1]) {
				for(int j=2; j<6; j++) line[0][j] = polygonPixels[i][j];
			}
			
			if (polygonPixels[i][0] == line[line.length-1][0] && polygonPixels[i][1] == line[line.length-1][1]) {
				for(int j=2; j<6; j++) line[line.length-1][j] = polygonPixels[i][j];;
			}
					
		}
		
		
		line[0][2] = Math.round(line[0][2]*100f)/100f;
		line[line.length-1][2] = Math.round(line[line.length-1][2]*100f)/100f;
		// Interpolate Z values along line
		float zInc = Math.round(((line[line.length-1][2]-line[0][2])/(line.length-1f))*100f)/100f;
		float rInc = (line[line.length-1][3]-line[0][3])/(line.length-1f);
		float gInc = (line[line.length-1][4]-line[0][4])/(line.length-1f);
		float bInc = (line[line.length-1][5]-line[0][5])/(line.length-1f);
		
		for(int i=1; i<line.length-1; i++) {
			line[i][2] = line[i-1][2] + zInc;
			line[i][3] = line[i-1][3] + rInc;
			line[i][4] = line[i-1][4] + gInc;
			line[i][5] = line[i-1][5] + bInc;	
		}
		
		return line;
	}
}
