/*
 * SYSC3033 Project Group 11 Server Part
 * 
 * Server entry point
 * */

package Server;

import java.io.*;

public class Server{
	
	private final int port = 69;
	private Controller myControl;
	
	public Server(){}

	public void start() throws IOException {
		myControl = new Controller(port);
		myControl.listenAndHandle();
	}

	//	Server starts here
	public static void main(String[] args) throws IOException{
		Server server = new Server();
		server.start();
	}
}
