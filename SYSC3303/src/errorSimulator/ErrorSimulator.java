/*
 * SYSC3303 Project G11 Error Simulator
*/
package errorSimulator;

import java.net.*;
import java.io.*;

public class ErrorSimulator{
	
	DatagramPacket receivedPacket;
	DatagramSocket sendReceiveSocket;
	
	/*
	 * Start the socket
	*/
	public void start() {
	
		try{
			sendReceiveSocket = new DatagramSocket(23);
		}catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
		while(true) {
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
		receivedPacket = new DatagramPacket(data, data.length);
		
		//	Wait for connections
		try {
			System.out.println("Listener : Waiting for request...");
			sendReceiveSocket.receive(receivedPacket);
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
		
		ESThread EST = new ESThread(receivedPacket);
		EST.start(); 
	}
	
	/*
	 * Error simulator starts here
	*/
	public static void main(String args[]) throws IOException{
		
		ErrorSimulator i = new ErrorSimulator();
				
		i.start();
		
	}

}
