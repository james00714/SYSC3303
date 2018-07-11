package Server;

import java.net.*;
import java.io.*;

public class Controller {
	
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket receivePacket, sendPacket;
	private FileHandler fileHandler;
	private RequestParser RP;
	private int blockNum;
	
	public Controller(int port) {		
		RP = new RequestParser();
		try{
			sendReceiveSocket = new DatagramSocket(port);
		}catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public void listenAndHandle() throws IOException {
		
		//Construct a DatagramPacket for receiving packets
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			System.out.println("\nServer : Waiting for request...");
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		displayReceived(receivePacket);
		RP.parseRequest(data);
		switch (RP.getType()) {
			case 1:	handleRead(RP.getFilename());
					break;
			case 2:	handleWrite(RP.getFilename());
					break;
			case 3:	handleData(RP.getBlockNum(), RP.getFileData());
					break;
			case 4:	handleACK();
					break;
			case 5:	handleERROR();
					break;
			case 6:	quit();
					break;
		}    
	}
	
	public void handleRead(String filename) throws IOException {
		System.out.print("File Read Requst Received.");
		System.out.print("Requested File: " + filename);
		System.out.print("Loading...");
		fileHandler = new FileHandler();
		blockNum = 1;
		SendDataPacket(fileHandler.readFile(filename), blockNum);
		blockNum++;
	}
	
	public void handleWrite(String filename) throws IOException {
		System.out.print("File Write Requst Received.");
		System.out.print("Prepare Writing File: " + filename);
		fileHandler = new FileHandler();
		fileHandler.prepareWrite(filename);
		blockNum = 0;
		SendDataPacket(new byte[0], blockNum);
		blockNum++;
	}
	
	public void handleData(int block, byte[] fileData) throws IOException {
		System.out.print("Data packet received.");
		System.out.print("New block received, writing...");
		if(blockNum == block) {
			fileHandler.writeFile(fileData);
			SendDataPacket(new byte[0], blockNum);
			if(fileData.length == 512) {	
				blockNum++;
			}else {
				fileHandler.close();
			}			
		}else {
			// wrong data packet received 
		}
	}
	
	public void handleACK() throws IOException {
		System.out.print("ACK packet Received.");
		SendDataPacket(fileHandler.readFile(), blockNum);
		blockNum++;
	}
	
	/*
	 *	Method to send data packet
	 *	If send ACK packet, leave data null
	 * 	In: data to send, block number
	 * */
	public void SendDataPacket(byte[] data, int blockNum) throws IOException {
		byte[] sendData = new byte[4 + data.length];
		sendData[0] = 0;
		sendData[1] = 3;
		sendData[2] = (byte)(blockNum / 256);
		sendData[3] = (byte)(blockNum % 256);
		for(int i = 0; i < data.length; i++) {
			sendData[4 + i] = data[i];
		}
		sendPacket = new DatagramPacket(sendData, sendData.length,
				receivePacket.getAddress(), receivePacket.getPort());
		try {
			displaySend(sendPacket);
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if(data.length < 512) {
			fileHandler.close();
		}
	}
	
	
	public void handleERROR() {
		System.out.print("ERROR packet Received.");
	}
	
	public void quit() {
		sendReceiveSocket.close();
		System.exit(1);
	}
	
	/*
	*	Method to display information related to the data received
	*	In: Received packet
	*/
	public void displayReceived(DatagramPacket receivePacket) {
		System.out.println("Server: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		System.out.println(new String(receivePacket.getData(),0,len));
		System.out.println();	
	}
	
	/*
	*	Method to display information related to the data that will be sent
	*/
	public void displaySend(DatagramPacket sendPacket) {
		System.out.println("Server : Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		System.out.println(new String(sendPacket.getData(),0,len));
	}

}
