/*
 * SYSC3033 Project Group 11 Server Part
 * 
 * RequestHandler to handle each request
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RequestHandler extends Thread{
	
	private static final int TIMEOUTMAX = 4;
	private static final String[] TYPES = {"RRQ", "WRQ", "DATA", "ACK", "ERROR"};
	
	private RequestParser RP;
	private DatagramPacket myPacket, sendPacket;
	private DatagramSocket sendReceiveSocket;
	private int length, finalBlock, TID;
	private Client myClient;
	private String ID, currentRequest;
	
	/*
	 * Construct handler with packet information
	 * */
	public RequestHandler(DatagramPacket receivePacket) throws IOException {
		
		myClient = null;
		myPacket = receivePacket;
		TID = receivePacket.getPort();
		ID = "No." + TID + ": ";
		
		try{
			sendReceiveSocket = new DatagramSocket();
		}catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
		
		System.out.println(ID + "New Connection Established");
		
	}
	
	/*
	 * New thread handling request from client starts here
	 * */
	public void run() {
		
		handleRequest();
	}
	
	/*
	 * Method to handle all requests
	 * */
	public void handleRequest() {
		if(myPacket.getPort() != TID) {
			System.out.println("ERROR: Unknown TID.");
			SendErrorPacket(5, "Unknown transfer ID.");
			try{
				receiveFromClient();
			}catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}			
		}else {
			RP = new RequestParser();
			length = myPacket.getLength();
			RP.parseRequest(myPacket.getData(), length);
			
			if(RP.ifCorrect()) {
				try{
					switch (RP.getType()) {
						case 1:	handleRead(RP.getFilename());
							break;
						case 2:	handleWrite(RP.getFilename());
							break;
						case 3:	handleData(RP.getBlockNum(), RP.getFileData());
							break;
						case 4:	handleACK(RP.getBlockNum());
							break;
						case 5:	handleERROR(RP.getErrorCode(), RP.getErrorMsg());
							break;
					}
				}catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				} 
			}else {
				SendErrorPacket(4, "Illegal TFTP operation.");
			}
		}
	}
	
	/*
	 * Method to handle read request
	 * In: filename to read
	 * */
	public void handleRead(String filename) throws IOException {
		
		System.out.println("File Read Requst Received.");
		System.out.println("Requested File: " + filename);
			
		if(myClient == null) {
			//	Save client information and send first piece of data
			myClient = new Client(myPacket, 1, new FileHandler(this));
			currentRequest = "READ";
			finalBlock = -1;
			byte[] filedata = myClient.getFileHandler().readFile(filename);	
			if(filedata == null) {
				System.out.println(ID + "Disconnected.");
				return;
			}
			System.out.println("Loading File...");
			if(filedata.length < 512) {
				finalBlock = myClient.getBlockNum();
			}
			SendDataPacket(filedata, myClient.getBlockNum());
			receiveFromClient();
		}else {
			//	ERROR WRQ RRQ not finished yet
			System.out.println("ERROR: WRQ RRQ not finished yet");
		}
	}

	/*
	 * Method to handle write request
	 * In: filename to write
	 * */
	public void handleWrite(String filename) throws IOException {
		
		System.out.println("File Write Requst Received.");
		
		//	Create Client and save information into it
		if(myClient == null) {
			myClient = new Client(myPacket, 1, new FileHandler(this));
			currentRequest = "WRITE";
			if(myClient.getFileHandler().prepareWrite(filename) == false) {
				System.out.println(ID + "Disconnected.");
				return;
			}
			sendACKPacket(myClient.getBlockNum() - 1);
			receiveFromClient();
		}else {
			//	ERROR WRQ RRQ not finished yet
			System.out.println("ERROR: WRQ RRQ not finished yet");
		}
	}
	
	/*
	 * Method to handle data request
	 * In: block number and data
	 * */
	public void handleData(int block, byte[] fileData) throws IOException {
		
		System.out.println("Data Packet Received.");
				
		if(myClient != null) {
			if(block == myClient.getBlockNum()){
				System.out.println("New Block Received, Writing...");
				if(myClient.getFileHandler().writeFile(fileData) == false) {
					System.out.println(ID + "Disconnected.");
					return;
				}
				myClient.incrementBlockNum();			
				sendACKPacket(myClient.getBlockNum() - 1);
				if(myPacket.getLength() < 516) {
					//	Reached the end of file
					System.out.println("Transfer Complete");
					System.out.println(ID + "Disconnected.");
					myClient.close();
				}else {
					receiveFromClient();
				}
			}else{
				System.out.println("ERROR: Ignoring wrong DATA package received.");
				receiveFromClient();
				//	Error
				//	Wrong ACK packet received
			}
		}else {
			System.out.println("ERROR: Unknown TID.");
			System.out.println(ID + "Disconnected.");
			SendErrorPacket(5, "Unknown transfer ID.");
		}
	}
	
	/*
	 * Method to handle ACK request
	 * In: filename to read
	 * */
	public void handleACK(int block) throws IOException {
		
		System.out.println("ACK Packet Received.");
		
		if(myClient != null) {
			if(block == myClient.getBlockNum()){
				if(finalBlock == block) {
					//	Client has received the last block
					//	End thread
					System.out.println("Transfer Complete");
					myClient.close();
				}else {
					myClient.incrementBlockNum();
					byte[] fileData = myClient.getFileHandler().readFile();
					if(fileData == null) return;
					if(fileData.length < 512) {
						finalBlock = myClient.getBlockNum();
					}
					SendDataPacket(fileData, myClient.getBlockNum());
					receiveFromClient();
				}
							
			}else{
				System.out.println("ERROR: Ignoring wrong ACK package received.");
				receiveFromClient();
			}
		}else {
			System.out.println("ERROR: Unknown TID.");
			System.out.println(ID + "Disconnected.");
			SendErrorPacket(5, "Unknown transfer ID.");
		}
	}
	
	/*
	 *	Method to send ACK packet with block number
	 * 	IN: block number
	 * */
	public void sendACKPacket(int blockNum) {
		
		//	Create byte array and set head bytes
		byte[] sendData = new byte[4];
		sendData[0] = 0;
		sendData[1] = 4;
		sendData[2] = (byte)(blockNum / 256);
		sendData[3] = (byte)(blockNum % 256);

		sendPacket(sendData);
	}
	
	/*
	 *	Method to send data packet with block number
	 *	If send ACK packet, pass a data with length 0
	 * 	In: data to send, block number
	 * */
	public void SendDataPacket(byte[] data, int blockNum) throws IOException {
    
		//	Create byte array and set head bytes
		byte[] sendData = new byte[4 + data.length];
		sendData[0] = 0;
		sendData[1] = 3;
		sendData[2] = (byte)(blockNum / 256);
		sendData[3] = (byte)(blockNum % 256);

		//	Save file data to data packet
		for(int i = 0; i < data.length; i++) {
			sendData[4 + i] = data[i];
		}
		
		sendPacket(sendData);
	}
	
	public void sendPacket(byte[] sendData) {
		
		sendPacket = new DatagramPacket(sendData, sendData.length,
				myPacket.getAddress(), myPacket.getPort());
		//	Send packet
		try {
			// displaySend(sendPacket);
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/*
	 *	Method to send error packet with code and message
	 * 	In: error code, error message
	 * */
	public void SendErrorPacket(int errorCode, String msg){
		
		System.out.println("Sending error packet, code " + errorCode);
		
		byte[] sendData = new byte[5 + msg.length()];
		byte[] msgData = msg.getBytes();
		sendData[0] = 0;
		sendData[1] = 5;
		sendData[2] = 0;
		sendData[3] = (byte) errorCode;
		for(int i = 0; i < msg.length(); i++) {
			sendData[4 + i] = msgData[i];
		}
		sendData[sendData.length - 1] = 0;
		
		sendPacket = new DatagramPacket(sendData, sendData.length,
				myPacket.getAddress(), myPacket.getPort());

		//	Send packet
		try {
			displaySend(sendPacket);
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void receiveFromClient() throws IOException {
		
		int terminate = 0;
		sendReceiveSocket.setSoTimeout(3000);
		
		try {
			sendReceiveSocket.receive(myPacket);
		} catch (SocketTimeoutException e) {
			terminate += 1;
			System.out.println("Time out " + terminate + ".");
			if(terminate == TIMEOUTMAX) {
				System.out.println(ID + "ERROR: No Response From Client, Disconnected.");
				return;
			}
			if(currentRequest.equals("WRITE")) {
				System.out.println("Resending...");
				sendReceiveSocket.send(sendPacket);
			}else {
				System.out.println("Waiting...");
			}
			receiveFromClient();
		}
		
		handleRequest();
	}
	
	public void handleERROR(int code, String msg) {
		System.out.println("ERROR packet Received.");
		System.out.println("Client ERROR : " + msg);
		System.out.println(ID + "Disconnected.");
	}
	
	public void displaySend(DatagramPacket packet) {
		System.out.println(ID + "\tSending packet...");
		System.out.println(ID + "\tDestination:\t" + packet.getAddress());
		System.out.println(ID + "\tPort:\t" + packet.getPort());
		System.out.println(ID + "\tType:\t" + TYPES[packet.getData()[1]]);
		System.out.println(ID + "\tLength:\t" + packet.getData().length);
	}
}
