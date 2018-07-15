/*
 * SYSC3033 Project Group 11 Server Part
 * 
 * RequestHandler to handle each request
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RequestHandler extends Thread{
	
	RequestParser RP;
	DatagramPacket myPacket, sendPacket;
	DatagramSocket sendSocket;
	int length;
	ClientManager myClientManager;
	
	/*
	 * Construct handler with packet information
	 * */
	public RequestHandler(DatagramPacket receivePacket, ClientManager clientManager, DatagramSocket socket) throws IOException {
		
		myPacket = receivePacket;
		sendSocket = socket;
		myClientManager = clientManager;
		length = receivePacket.getLength();
		RP = new RequestParser();
		RP.parseRequest(receivePacket.getData(), length);
	}
	
	/*
	 * New thread handling request from client starts here
	 * */
	public void run() {
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
	}
	
	/*
	 * Method to handle read request
	 * In: filename to read
	 * */
	public void handleRead(String filename) throws IOException {
		
		System.out.println("File Read Requst Received.");
		System.out.println("Requested File: " + filename);
		System.out.println("Loading...");
		
		if(myClientManager.getClientByPacket(myPacket) == null) {
			//	Create fileHandler and save file information to it
			Client newClient = new Client(myPacket, 1, new FileHandler());
			myClientManager.addClient(newClient);
			byte[] filedata = newClient.getFileHandler().readFile(filename);
			SendDataPacket(filedata, newClient.getBlockNum());
		}else {
			//	Error
			//	Current client does not finish transfer yet
		}	
	}

	/*
	 * Method to handle write request
	 * In: filename to write
	 * */
	public void handleWrite(String filename) throws IOException {
		
		System.out.println("File Write Requst Received.");
		
		if(myClientManager.getClientByPacket(myPacket) == null) {
			System.out.println("Prepare Writing File: " + filename);
			//	Create Client and save information into it
			Client newClient = new Client(myPacket, 1, new FileHandler());
			newClient.getFileHandler().prepareWrite(filename);
			myClientManager.addClient(newClient);
			SendDataPacket(new byte[0], newClient.getBlockNum() - 1);
		}else {
			//	Error
			//	Current client does not finish transfer yet
		}	
	}
	
	/*
	 * Method to handle data request
	 * In: block number and data
	 * */
	public void handleData(int block, byte[] fileData) throws IOException {
		
		System.out.println("Data packet received.");
				
		Client c = myClientManager.getClientByPacket(myPacket);
		if(c != null) {
			if(block == c.getBlockNum()){
				System.out.println("New block received, writing...");
				c.getFileHandler().writeFile(fileData);
				if(myPacket.getLength() < 516) {
					//	Reached the end of file
					myClientManager.removeClient(myPacket);
				}else {
					c.incrementBlockNum();
				}
				SendDataPacket(new byte[0], c.getBlockNum() - 1);
			}else{
				System.out.println("ERROR: Wrong package received.");
				//	Error
				//	Wrong ACK packet received
			}
		}else {
			System.out.println("ERROR: Unknown TID.");
			//	Error
			//	Unknown TID
		}
	}
	
	/*
	 * Method to handle ACK request
	 * In: filename to read
	 * */
	public void handleACK(int block) throws IOException {
		System.out.println("ACK packet Received.");
		Client c = myClientManager.getClientByPacket(myPacket);
		if(c != null) {
			if(block == c.getBlockNum()){
				c.incrementBlockNum();
				SendDataPacket(c.getFileHandler().readFile(), c.getBlockNum());
			}else{
				System.out.println("ERROR: Wrong package received.");
				//	Error
				//	Wrong ACK packet received
			}
		}else {
			System.out.println("ERROR: Unknown TID.");
			//	Error
			//	Unknown TID
		}
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
		if(data.length == 0){
			sendData[1] = 4;
		}else{
			sendData[1] = 3;
		}
		sendData[2] = (byte)(blockNum / 256);
		sendData[3] = (byte)(blockNum % 256);

		//	Save file data to data packet
		for(int i = 0; i < data.length; i++) {
			sendData[4 + i] = data[i];
		}
		sendPacket = new DatagramPacket(sendData, sendData.length,
				myPacket.getAddress(), myPacket.getPort());

		//	Send packet
		try {
			// displaySend(sendPacket);
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//	Close fileHandler if reached the end
		if(data.length != 0){
			if(data.length < 512) {
				myClientManager.removeClient(myPacket);
			}
		}
	}
	
	/*
	 *	Method to send error packet with code and message
	 * 	In: error code, error message
	 * */
	public void SendErrorPacket(int errorCode, String msg){
		
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
			// displaySend(sendPacket);
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void handleERROR(int code, String msg) {
		System.out.println("ERROR packet Received.");
	}
}
