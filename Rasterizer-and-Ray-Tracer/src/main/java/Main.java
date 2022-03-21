package main.java;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		RenderObject object = new RenderObject();
		
		try {
			object.loadPLY();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Complete");
	}

}
