package ErrorSimulator;

import java.io.*;
import java.net.*;

public class ESThreadNormal implements Runnable{
	
	private DatagramSocket sendReceiveSocket;
	
	private DatagramPacket receivePacket, sendPacket;
	
	private int tID;
	private int clientPort;
	//private InetAddress clientAddress;
	
	
	public ESThreadNormal(int tID, DatagramPacket clientPacket) {
		
		this.tID = tID;
		this.receivePacket = clientPacket;
		this.clientPort = receivePacket.getPort();
		//this.clientAddress = receivePacket.getAddress();
		
		Thread t = new Thread(this, Integer.toString(this.tID));
		
		t.start();
	}
	
	public void run() {
		
		
		try {
			
			sendReceiveSocket = new DatagramSocket(tID);
			
		}catch(SocketException se) {
			
			se.printStackTrace();
		    System.exit(1);
		}
		
		receiveFromServer();
		
		while(true) { // need to change the stop condition
			receiveFromClient();
			receiveFromServer();
		}
	}
	
	private void receiveFromClient() {
		
		
		byte data[] = new byte[1024];
		
		receivePacket = new DatagramPacket(data, data.length);
		
		System.out.println("Error Simulator: Waiting for Packet from client.\n");

		
		// Block until a datagram packet is received from receiveSocket.
		
		try {

			sendReceiveSocket.receive(receivePacket);

		    } catch (IOException e) {

		       System.out.print("IO Exception: likely:");
		       System.out.println("Receive Socket Timed Out.\n" + e);

		       e.printStackTrace();
		       sendReceiveSocket.close();
		       System.exit(1);
		    }
		
		// Process the received datagram.
		
		System.out.println("Error Simulator: Packet received from client:");
	    System.out.println("From host: " + receivePacket.getAddress());
	    System.out.println("host port: " + receivePacket.getPort());

	    int len = receivePacket.getLength();
	    System.out.println("Length: " + len);
	    printMessage(receivePacket.getData(), len);
		
	    
	    // sendPacket, send to server
	    
	    try {
	    	
	    	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), 69); 
		    sendReceiveSocket.send(sendPacket);
		    
	    }catch(IOException e) {
	    	
	    	e.printStackTrace();
	    	sendReceiveSocket.close();
	    	System.exit(1);
		    
	    }
	    
	    System.out.println("Error Simulator: Sending packet to Server:");
	    System.out.println("To host: " + sendPacket.getAddress());
	    System.out.println("Destination host port: " + sendPacket.getPort());

	    len = sendPacket.getLength();
	    System.out.println("Length: " + len);
	    printMessage(sendPacket.getData(),len);
	    
	}
	
	private void receiveFromServer() {
		
		
		byte data[] = new byte[1024];
		
		receivePacket = new DatagramPacket(data, data.length);
		
		System.out.println("Error Simulator: Waiting for Packet from server.\n");

		
		// Block until a datagram packet is received from receiveSocket.
		
		try {

			sendReceiveSocket.receive(receivePacket);

		    } catch (IOException e) {

		       System.out.print("IO Exception: likely:");
		       System.out.println("Receive Socket Timed Out.\n" + e);

		       e.printStackTrace();
		       sendReceiveSocket.close();
		       System.exit(1);
		    }
		
		// Process the received datagram.
		
		System.out.println("Error Simulator: Packet received from server:");
	    System.out.println("From host: " + receivePacket.getAddress());
	    System.out.println("host port: " + receivePacket.getPort());

	    int len = receivePacket.getLength();
	    System.out.println("Length: " + len);
	    printMessage(receivePacket.getData(), len);
		
	    
	    // sendPacket, send to server
	    
	    try {
	    	
	    	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), clientPort); 
		    sendReceiveSocket.send(sendPacket);
		    
	    }catch(IOException e) {
	    	
	    	e.printStackTrace();
	    	sendReceiveSocket.close();
	    	System.exit(1);
		    
	    }
	    
	    System.out.println("Error Simulator: Sending packet to Server:");
	    System.out.println("To host: " + sendPacket.getAddress());
	    System.out.println("Destination host port: " + sendPacket.getPort());

	    len = sendPacket.getLength();
	    System.out.println("Length: " + len);
	    printMessage(sendPacket.getData(),len);
	    
	}
	
	public void printMessage(byte[] data, int len) {

		  RequestParser rp = new RequestParser();


		  rp.parseRequest(data, len);


		    if(rp.getType()==1) {

		    	System.out.println("***Parse Read Request***");
		    	System.out.print("Containing: " );
		    	System.out.print("filename: " + rp.getFilename());

		    }else if(rp.getType()==2) {

		    	System.out.println("***Parse Write Request***");
		    	System.out.print("Containing: " );
		    	System.out.println("filename: " + rp.getFilename());

		    }else if(rp.getType()==3) {

		    	System.out.println("***Parse Data***");
		    	System.out.print("Containing: ");
		    	System.out.println(rp.getBlockNum());
		    	System.out.println(rp.getFileData().toString());

		    }else if(rp.getType()==4) {

		    	System.out.println("***Parse ACK***");
		    	System.out.print("Containing: ");
		    	System.out.print(rp.getBlockNum());

		    }

	  }

}
