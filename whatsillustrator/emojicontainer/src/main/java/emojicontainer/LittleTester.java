package emojicontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LittleTester {
	
	public LittleTester() {
		/*InputStream in = this.getClass().getResourceAsStream("/Andanotherone.txt");
		System.out.println(in);*/
		
		try (
	            InputStream in = this.getClass().getResourceAsStream("/*");
	            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
	        String resource;

	        while ((resource = br.readLine()) != null) {
	            System.out.println("Resource " + resource);
	        }
	    } catch(IOException ioe) {
	    	System.out.println(ioe);
	    }
	}

	public static void main(String[] args) {
		System.out.println("Hello!");
		new LittleTester();
	}

}
