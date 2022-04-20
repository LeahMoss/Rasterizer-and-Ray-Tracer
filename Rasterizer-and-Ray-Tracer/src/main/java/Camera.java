package main.java;

public class Camera {
	
	// Identity rotation
	public float[][] RI = {{1,0,0}, 
		 				   {0,1,0}, 
		 				   {0,0,1}}; 
	
	// Rotation in x of 90 degrees
	public float[][] Rx90 = {{1,0,0}, 
		 				      {0,0,-1}, 
		 				      {0,1,0}}; 
	
	// Rotation in y of 90 degrees
	public float[][] Ry90 = {{0,0,1}, 
			 				  {0,1,0}, 
			 				  {-1,0,0}}; 
	
	// Rotation in y of 90 degrees
	public float[][] Rz90 = {{0,-1,0}, 
				 			  {1,0,0}, 
				 			  {0,0,1}};
	
	// Rotation in x of 45 degrees
	public float[][] Rx45 = {{1,0,0}, 
							 {0,(float) Math.cos(Math.PI/4),(float) -Math.sin(Math.PI/4)}, 
							 {0,(float) Math.sin(Math.PI/4),(float) Math.cos(Math.PI/4)}}; 

	// Rotation in y of 45 degrees
	public float[][] Ry45 = {{(float) Math.cos(Math.PI/4),0,(float) Math.sin(Math.PI/4)}, 
							 {0,1,0}, 
							 {(float) -Math.sin(Math.PI/4),0,(float) Math.cos(Math.PI/4)}}; 

	// Rotation in y of 45 degrees
	public float[][] Rz45 = {{(float) Math.cos(Math.PI/4),(float) -Math.sin(Math.PI/4),0}, 
						 	 {(float) Math.sin(Math.PI/4),(float) Math.cos(Math.PI/4),0}, 
						 	 {0,0,1}};
	
	// Rotation in x of 45 degrees
		public float[][] Rx225 = {{1,0,0}, 
								 {0,(float) Math.cos(Math.PI/8),(float) -Math.sin(Math.PI/8)}, 
								 {0,(float) Math.sin(Math.PI/8),(float) Math.cos(Math.PI/8)}}; 

		// Rotation in y of 45 degrees
		public float[][] Ry225 = {{(float) Math.cos(Math.PI/8),0,(float) Math.sin(Math.PI/8)}, 
								 {0,1,0}, 
								 {(float) -Math.sin(Math.PI/8),0,(float) Math.cos(Math.PI/8)}}; 

		// Rotation in y of 45 degrees
		public float[][] Rz225 = {{(float) Math.cos(Math.PI/8),(float) -Math.sin(Math.PI/8),0}, 
							 	 {(float) Math.sin(Math.PI/8),(float) Math.cos(Math.PI/8),0}, 
							 	 {0,0,1}};
		
	// Flip
	public float[][] RF = {{1,0,0}, 
						   {0,-1,0}, 
						   {0,0,-1}}; 
	
	private float[][] R = {{1,0,0}, 
						   {0,1,0}, 
						   {0,0,1}};	
				
	// Camera intrinsics with f=1, cx=0, cy=0;
	private float[][] K = {{1,0,0,0},
						   {0,1,0,0},
						   {0,0,1,0}};
		 								 
	private float[] t = new float[3];
	
	private float f, cx, cy;
	
	/*
	 * Initialises a camera to face an object
	 * 
	 * @param vertices Objects vertices
	 * @param width Width of desired image
	 * @param height Height of desired image
	 * @param flip True if the render is initially upside down
	 */
	public void calibrate(float[][] vertices, float width, float height, boolean flip) {
		if (flip) this.R = matMul(this.R,this.RF);
		
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
		
		// Calculate t matrix
		float xT = -0.5f*(min[0]+max[0]);
		float yT =  -0.5f*(min[1]+max[1]);
		float zT = -0.5f*(min[2]+max[2]);
		this.t[0] = (this.R[0][0]*xT) + (this.R[0][1]*yT) + (this.R[0][2]*zT);
		this.t[1] = (this.R[1][0]*xT) + (this.R[1][1]*yT) + (this.R[1][2]*zT);
		this.t[2] = (this.R[2][0]*xT) + (this.R[2][1]*yT) + (this.R[2][2]*zT) + (4*distance);
		
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
		
		if (width < height) {
			this.f = (width/width2d);
		}
		else {
			this.f = (height/height2d);
		}
		
		this.cx = width/2;
		this.cy = height/2;
				
		this.K[0][0] = f;
		this.K[1][1] = f;
		this.K[0][2] = cx;
		this.K[1][2] = cy;
		
	}

	/**
	* Performs perspective projection on a given list of vertices using
	* given camera parameters.
	*
	* @param vertices Vertices to project
	* @return projected Transformed pixels
	*/
	public float[][] projectToCameraCoords(float[][] vertices) {
		
		float[][] R_t = {{this.R[0][0],this.R[0][1],this.R[0][2],this.t[0]},
						 {this.R[1][0],this.R[1][1],this.R[1][2],this.t[1]},
						 {this.R[2][0],this.R[2][1],this.R[2][2],this.t[2]},
						 {0,0,0,1}};
		float[][] KR_t = new float[3][4];
		float[][] transformed = new float[vertices.length][3];
		float[][] projected = new float[vertices.length][3];
		
		// K * R_t		
		KR_t = matMul(K, R_t);
		
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
				// For use by Z buffer later
				projected[n][2] = transformed[n][2];
			}
		}
		
		return projected;
	}
	
	/*
	 * Multiplies 2 matrices
	 * 
	 * @param mat1 Left hand matrix
	 * @param mat2 Right hand matrix
	 * @return result Product of mat1 x mat2
	 */
	public float[][] matMul(float[][] mat1, float[][] mat2) {
		float[][] result = new float[mat1.length][mat2[0].length];
		
		for(int i=0; i<mat1.length; i++) {
			for (int j=0; j<mat2[0].length; j++) {
				for(int k=0; k<mat1[0].length; k++) {
					result[i][j] += (mat1[i][k]*mat2[k][j]);
				}
			}
		}
		
		return result;
	}
	
	/*
	 * @return cx
	 */
	public float getCx() {
		return this.cx;
	}
	
	/*
	 * @return xy
	 */
	public float getCy() {
		return this.cy;
	}
	
	/*
	 * @param newR New value for R
	 */
	public void setR(float[][] newR) {
		this.R = newR;
	}
}
