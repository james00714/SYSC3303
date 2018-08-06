/*
 * SYSC3033 Project Group 11 Server Part
 * 
 * Server entry point
 * */

package server;

import java.io.*;
import java.util.Scanner;

public class Server{
	
	private final int port = 69;  // Port number
	private Listener myListener;
	
	public Server(){}

	public void start() throws IOException {
		
		myListener = new Listener(port);
		Scanner sc = new Scanner (System.in);
		String cmd;
		myListener.start();
		
		System.out.println("Server now online, input \"quit\" to end the server.");
		
		while(true) {
			cmd = sc.next();
			if(cmd.toLowerCase().equals("quit")) {
				stopServer();
				sc.close();
				break;
			}else if(cmd.toLowerCase().equals("dir")){
				System.out.println("Please provide the directory: ");
				String dir = sc.next();
				while(true) {
					if(dir.equals("default")) {
						FileHandler.setDefaultDir();
						System.out.println("Working directory set to default.");
						break;
					}else {
						File f = new File(dir);
						if(f.isDirectory()) {
							FileHandler.setDir(dir);
							System.out.println("Working directory set to " + dir);
							break;
						}else {
							System.out.println("Directory not exists, please try again.");
							System.out.println("Please provide the directory: ");
							dir = sc.next();
						}
					}
				}		
			}else {
				System.out.println("Invalid input.");
			}
		}
		System.out.println("Server has been terminated.");
	}

	/*
	 * Method to stop the server
	 * */
	public synchronized void stopServer() {
		myListener.stopRunning();  // Stop listener
		Client.closeAll();
	}
	
	//	Server starts here
	public static void main(String[] args) throws IOException{
		
		Server server = new Server();
		server.start();
	}
}
