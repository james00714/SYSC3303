/*
 * RequestHandler to handle each request
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RequestHandler extends Thread{
	
	private static final int TIMEOUTMAX = 4;
	private static final String[] TYPES = {"RRQ", "WRQ", "DATA", "ACK", "ERROR"};
	
	private RequestParser RP;
	private DatagramPacket myPacket, sendPacket;
	private DatagramSocket sendReceiveSocket;
	private int length, finalBlock, TID;
	private int terminate = 0;
	private Client myClient;
	private String ID, currentRequest = "";
	private InetAddress TAddr;
	private boolean continueListen = true;
	
	/*
	 * Construct handler with packet information
	 * */
	public RequestHandler(DatagramPacket receivePacket) throws IOException {
		
		super(String.valueOf(receivePacket.getPort()));
		myClient = null;
		myPacket = receivePacket;
		TID = receivePacket.getPort();
		TAddr = receivePacket.getAddress();
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
		
		while(continueListen) {
			try {
				receiveFromClient();
				if(continueListen == false) return;
				handleRequest();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(ID + "Disconnected.");
	}
	
	/*
	 * Method to handle all requests
	 * */
	public void handleRequest() {
		if(myPacket.getPort() != TID || myPacket.getAddress() != TAddr) {
			System.out.println(ID + "ERROR: Unknown TID.");
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
		
		System.out.println(ID + "File Read Requst Received.");
		System.out.println(ID + "Requested File: " + filename);
		System.out.println(ID + "Transfering...");
		
		if(myClient == null) {
			//	Save client information and send first piece of data
			myClient = new Client(myPacket, 1, new FileHandler(this));
			if(Client.addToClients(myClient) == false) {
				System.out.println(ID + "ERROR: Invalid request, the client is currently active.");
				continueListen = false;
				return;
			}

			currentRequest = "READ";
			finalBlock = -1;
			byte[] filedata = myClient.getFileHandler().readFile(filename);	
			if(filedata == null) {
				myClient.close();
				continueListen = false;
				return;
			}
			Printer.printInfo(ID + "Loading File...");
			if(filedata.length < 512) {
				finalBlock = myClient.getBlockNum();
			}
			SendDataPacket(filedata, myClient.getBlockNum());
		}else {
			//	ERROR WRQ RRQ not finished yet
			System.out.println(ID + "ERROR: Previous WRQ/RRQ not finished yet");	
			System.out.println(ID + "Ignoring RRQ received");	
		}
		
	}

	/*
	 * Method to handle write request
	 * In: filename to write
	 * */
	public void handleWrite(String filename) throws IOException {
		
		System.out.println(ID + "File Write Requst Received.");
		System.out.println(ID + "Requested File: " + filename);
		System.out.println(ID + "Transfering...");
		
		//	Create Client and save information into it
		if(myClient == null) {
			myClient = new Client(myPacket, 1, new FileHandler(this));
			if(Client.addToClients(myClient) == false) {
				System.out.println(ID + "ERROR£ºInvalid request, the client is currenly active.");
				continueListen = false;
				return;
			}
			currentRequest = "WRITE";
			if(myClient.getFileHandler().prepareWrite(filename) == false) {
				myClient.close();
				continueListen = false;
				return;
			}
			sendACKPacket(myClient.getBlockNum() - 1);
			
		}else {
			//	ERROR WRQ RRQ not finished yet
			System.out.println(ID + "ERROR: Previous WRQ/RRQ not finished yet");	
			System.out.println(ID + "Ignoring WRQ received");	
		}
	}
	
	/*
	 * Method to handle data request
	 * In: block number and data
	 * */
	public void handleData(int block, byte[] fileData) throws IOException {
		
		Printer.printInfo(ID + "Data Packet Received.");
		Printer.printInfo(ID + "Block#: " + block);
		Printer.printInfo(ID + "Data length: " + fileData.length);
		
		if(myClient != null) {
			if(block == myClient.getBlockNum()){
				Printer.printInfo(ID + "Writing...");
				if(myClient.getFileHandler().writeFile(fileData) == false) {
					myClient.close();
					continueListen = false;
					return;
				}
				myClient.incrementBlockNum();			
				sendACKPacket(myClient.getBlockNum() - 1);
				if(myPacket.getLength() < 516) {
					//	Reached the end of file
					System.out.println(ID + "Transfer Complete");
					myClient.close();
					continueListen = false;
					return;
				}
			}else if(block == (myClient.getBlockNum() - 1)){
				System.out.println(ID + "ERROR: Previous data block received, resending ACK packet...");
				sendPacket(sendPacket);
			}else{
				System.out.println(ID + "ERROR: Ignoring wrong DATA package received.");
			}
		}else {
			System.out.println(ID + "ERROR: Unknown TID.");
			SendErrorPacket(5, "Unknown transfer ID.");
		}
	}
	
	/*
	 * Method to handle ACK request
	 * In: filename to read
	 * */
	public void handleACK(int block) throws IOException {
		
		Printer.printInfo(ID + "ACK Packet Received.");
		Printer.printInfo(ID + "Block#: " + block);
		
		if(myClient != null) {
			if(block == myClient.getBlockNum()){
				if(finalBlock == block) {
					//	Client has received the last block
					//	End thread
					System.out.println(ID + "Transfer Complete");
					myClient.close();
					continueListen = false;
					return;
				}else {
					myClient.incrementBlockNum();
					byte[] fileData = myClient.getFileHandler().readFile();
					if(fileData == null) {
						myClient.close();
						continueListen = false;
						return;
					}
					if(fileData.length < 512) {
						finalBlock = myClient.getBlockNum();
					}
					SendDataPacket(fileData, myClient.getBlockNum());
				}
			}else{
				System.out.println(ID + "ERROR: Ignoring wrong ACK package received.");
			}
		}else {
			System.out.println(ID + "ERROR: Unknown TID.");
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
		sendPacket(sendPacket);
	}
	
	public void sendPacket(DatagramPacket packet) {
		//	Send packet
		try {
			displaySend(sendPacket);
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *	Method to send error packet with code and message
	 * 	In: error code, error message
	 * */
	public void SendErrorPacket(int errorCode, String msg){
		
		System.out.println(ID + "Sending ERROR packet, code: " + errorCode);
		System.out.println(ID + "ERROR message: " + msg);
		
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
		sendPacket(sendData);
	}
	
	public void receiveFromClient() throws IOException {
		
		sendReceiveSocket.setSoTimeout(3000);
		
		try {
			sendReceiveSocket.receive(myPacket);
			terminate = 0;
		} catch (SocketTimeoutException e) {
			terminate += 1;
			System.out.println(ID + "Time out " + terminate + ".");
			
			//	close connection
			if(terminate == TIMEOUTMAX) {
				System.out.println(ID + "ERROR: No Response From Client.");
				if(myClient != null) myClient.close();
				continueListen = false;
				return;
			}
			
			//	if current request is RRQ, send previous data packet again
			if(currentRequest.equals("READ")) {
				System.out.println(ID + "Resending previous DATA packet...");
				sendReceiveSocket.send(sendPacket);
			}else {
				System.out.println("Waiting...");
			}
			receiveFromClient();
		}
	}
	
	public void handleERROR(int code, String msg) {
		System.out.println(ID + "ERROR packet Received.");
		System.out.println(ID + "ERROR code: " + code);
		System.out.println(ID + "ERROR message: " + msg);
		if(myClient != null)
			try {
				myClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		continueListen = false;
	}
	
	public void displaySend(DatagramPacket packet) {
		Printer.printInfo(ID + "\tSending packet...");
		Printer.printInfo(ID + "\tDestination:\t" + packet.getAddress());
		Printer.printInfo(ID + "\tPort:\t" + packet.getPort());
		Printer.printInfo(ID + "\tType:\t" + TYPES[packet.getData()[1] - 1]);
		Printer.printInfo(ID + "\tPacket Length:\t" + packet.getData().length);
		if(packet.getData()[1] == 3) {
			Printer.printInfo(ID + "\tBlock#:\t" + RequestParser.parseBlockNum(packet.getData()[2], packet.getData()[3]));
			Printer.printInfo(ID + "\tData length:\t" + (packet.getLength() - 4));
		}else if(packet.getData()[1] == 4) {
			Printer.printInfo(ID + "\tBlock#:\t" + RequestParser.parseBlockNum(packet.getData()[2], packet.getData()[3]));
		}
	}
}
