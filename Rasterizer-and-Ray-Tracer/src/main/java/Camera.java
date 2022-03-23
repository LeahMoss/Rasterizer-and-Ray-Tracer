package main.java;

public class Camera {
	
	private float[][] R = new float[][] {{1,0,0}, 
		 								 {0,1,0}, 
		 								 {0,0,1}}; 
		 								 
	private float[][] K = new float[][] {{1,0,0,0},
		 								 {0,1,0,0},
		 								 {0,0,1,0}};
		 								 
	private float[] t;
	
	private float f, cx, cy;
	
	public Camera(float[][] vertices, float width, boolean flip) {
		// Find the minimum and maximum x,y,z
		float[] min = vertices[0].clone(), max = vertices[0].clone();
		
		for(int col=0; col<3; col++) {
			for(int row=1; row<vertices.length; row++) {
				
				if(vertices[row][col] < min[col]) {
					min[col] = vertices[row][col];
				}
				
				if (vertices[row][col] > max[col]) {
					max[col] = vertices[row][col];
				}
			}
		}
		
		// Calculate the diagonal of bounding box
		float distance = (float) Math.sqrt(Math.pow(max[0]-min[0], 2) +
											Math.pow(max[1]-min[1], 2) +
											Math.pow(max[2]-min[2], 2));
		float halfway = -0.5f*distance;
		
		// Translate camera to centre of image, 3x away from object
		this.t = new float[] {halfway, halfway, halfway+(3*distance)};
		
		// Initial project to camera coordinates using default R and K
		float[][] projected = projectToCameraCoords(vertices);
		
		// Find the maximum x,y values of project vertices
		float maxX = projected[0][0], minX = projected[0][0], 
				maxY = projected[0][1], minY = projected[0][1];
		
		for (int i=0; i<projected.length; i++) {
			if (projected[i][0] > maxX) {
				maxX = projected[i][0];
			}
			if (projected[i][0] < minX) {
				minX = projected[i][0];
			}
			if (projected[i][1] > maxY) {
				maxY = projected[i][1];
			}
			if (projected[i][0] < minY) {
				minY = projected[i][1];
			}
		}
		
		float width2d = 2*Math.max(maxX, Math.abs(minX));
		float height2d = 2*Math.max(maxY, Math.abs(minY));
		float aspect = height2d/width2d;
		
		this.f = width/width2d;
		this.cx = width/2;
		this.cy = (float) (Math.ceil(width*aspect)/2);
		
		this.K[0][0] = f;
		this.K[1][1] = f;
		this.K[0][2] = cx;
		this.K[1][2] = cy;
		
		//float[][] finalProjected = project(vertices);
	}

	/**
	* Performs perspective projection on a given list of vertices using
	* given camera parameters.
	*
	* @param vertices
	* @return transformed pixels
	*/
	public float[][] projectToCameraCoords(float[][] vertices) {
		
		float[][] R_t = {{R[0][0],R[0][1],R[0][2],t[0]},
						 {R[1][0],R[1][1],R[1][2],t[1]},
						 {R[2][0],R[2][1],R[2][2],t[2]},
						 {0,0,0,1}};
		float[][] KR_t = new float[K.length][R_t[0].length];
		float[][] transformed = new float[vertices.length][3];
		float[][] projected = new float[vertices.length][2];
		
		// K * R_t
		for(int i=0; i<K.length; i++) {
			for (int j=0; j<R_t[0].length; j++) {
				KR_t[i][j] = (K[i][0]*R_t[0][j] + 
								K[i][1]*R_t[1][j] + 
								K[i][2]*R_t[2][j] + 
								K[i][3]*R_t[3][j]);
			}
		}
		
		// (K * R_t) * Vertices
		for (int n=0; n<vertices.length; n++) {
			for(int i=0; i<KR_t.length; i++) {
				transformed[n][i] = (KR_t[i][0] * vertices[n][0] +
						KR_t[i][1] * vertices[n][1] +
						KR_t[i][2] * vertices[n][2]) +
						KR_t[i][3] * 1;
			}
			
			if (transformed[n][2] != 0) {
				projected[n][0] = transformed[n][0]/transformed[n][2];
				projected[n][1] = transformed[n][1]/transformed[n][2];
			}
		}
		
		return projected;
	}
	
	public float getCx() {
		return this.cx;
	}
	
	public float getCy() {
		return this.cy;
	}
	
	public float getF() {
		return this.f;
	}
}
