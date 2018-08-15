/*
 * Controller class to listen and handle request from client
 * */

package server;

import java.net.*;
import java.io.*;

public class Listener extends Thread{
	
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket receivePacket;
	
	private boolean running = true;		// turn to false if needs to close listener 
	
	public Listener(int port) {		
		try{
			sendReceiveSocket = new DatagramSocket(port);
		}catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	/*
	 * New thread(listener) starts here
	 * */
	public void run() {
		
		while(running) {
			try {
				listenAndHandle();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Method to listen request and handle it
	 * */
	public void listenAndHandle() throws IOException {
		
		//	Construct a DatagramPacket for receiving packets
		byte data[] = new byte[1024];
		receivePacket = new DatagramPacket(data, data.length);
		
		//	Wait for connections
		try {
			System.out.println("\nListener : Waiting for request...");
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			if(e.getMessage().equals("socket closed")) {
				System.out.println("Socket has been closed.");
			}else {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();	
				System.out.println();
				System.exit(1);
			}		
		}
		
		
		if(receivePacket.getPort() != -1){
			displayReceived(receivePacket);
			
			//	Parse the request and handle it with a new thread
			RequestHandler RH = new RequestHandler(receivePacket);
			RH.start(); 
		}	
	}
	
	/*
	*	Method to terminate listener and close the socket
	*/
	public void stopRunning() {
		System.out.println("Closing listener...");
		running = false;
		sendReceiveSocket.close();	
	}
	
	
	
	/*
	*	Method to display information related to the data received
	*	In: Received packet
	*/
	public void displayReceived(DatagramPacket receivePacket) {
		
		Printer.printInfo("Server: Packet received:");
		Printer.printInfo("From host: " + receivePacket.getAddress());
		Printer.printInfo("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		Printer.printInfo("Packet Length: " + len);
	}
}
