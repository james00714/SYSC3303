package ErrorSimulator;

import java.io.*;
import java.net.*;
import java.util.Random;

public class ErrorSController{
	
	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	
	private int errorSPort = 2000; //should be 23

	
	public ErrorSController(){
		try {
			
			receiveSocket = new DatagramSocket(errorSPort);
			
		}catch(SocketException se) {
			
			se.printStackTrace();
		    System.exit(1);
		}
	}
	
	public void distribute(int errorCode) {
		
		System.out.println("Error Simulator: Waiting for Packet from client.\n");
		
		byte data[] = new byte[1024];
		receivePacket = new DatagramPacket(data, data.length);
		
		try {

			receiveSocket.receive(receivePacket);

		    } catch (IOException e) {

		       System.out.print("IO Exception: likely:");
		       System.out.println("Receive Socket Timed Out.\n" + e);

		       e.printStackTrace();
		       receiveSocket.close();
		       System.exit(1);
		    }
	    
		
		switch(errorCode) {
		
			case 0:
				System.out.println("error code: 0");
				break;
			case 1:
				System.out.println("error code: 1");
				break;
			case 2:
				System.out.println("error code: 2");
				break;
			case 3:
				System.out.println("error code: 3");
				break;
			case 4:
				System.out.println("error code: 4");
				break;
			case 5:
				System.out.println("error code: 5");
				break;
			case 6:
				System.out.println("error code: 6");
				break;
			case 7:
				System.out.println("error code: 7");
				break;
			case 8:
				System.out.println("error code: 8 [Normal]");
				ESThreadNormal normal = new ESThreadNormal(receiveSocket, receivePacket);
				break;
			case 9:
				System.out.println("error code: 9");
				System.out.println("Quiting...");
				break;
			default:
				System.out.println("Something is wrong...");
			
		}

	}


}
