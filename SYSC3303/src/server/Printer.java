/*
 * Printer to print information
 * */

package server;

public class Printer {

	private static int mode = 0;
	
	public static void setMode(int i) {
		mode = i;
	}
	
	public static void printInfo(String info) {
		if(mode == 0)
			System.out.println(info);
	}
}
