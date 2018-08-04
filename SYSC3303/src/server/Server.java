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
			}else {
				System.out.println("Invalid input.");
			}
		}
		System.out.println("Server has been terminated.");
	}

	/*
	 * Method to stop the server
	 * */
	public void stopServer() {
		myListener.stopRunning();  // Stop listener
		while(Client.getClientsSize() != 0) {
			try {
				wait();	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
	
	//	Server starts here
	public static void main(String[] args) throws IOException{
		
		Server server = new Server();
		server.start();
	}
}
