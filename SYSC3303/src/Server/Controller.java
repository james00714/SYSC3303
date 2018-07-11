/*
 * Controller class to listen and handle request from client
 * */

package Server;

import java.net.*;
import java.io.*;

public class Controller {
	
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket receivePacket, sendPacket;
	private FileHandler fileHandler;	// holds information of operating file
	private RequestParser RP;			// holds information of current request

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
	
	/*
	 * Method to listen request and handle it
	 * */
	public void listenAndHandle() throws IOException {
		
		//	Construct a DatagramPacket for receiving packets

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
	
		//	Parse the request and handle it 
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
		
		//	Create fileHandler and save file information to it
		fileHandler = new FileHandler();
		blockNum = 1;
		SendDataPacket(fileHandler.readFile(filename), blockNum);
		blockNum++;
	}

	/*
	 * Method to handle write request
	 * In: filename to write
	 * */
	public void handleWrite(String filename) throws IOException {
		
		System.out.print("File Write Requst Received.");
		System.out.print("Prepare Writing File: " + filename);
		fileHandler = new FileHandler();
		//	Prepare the file to write
		fileHandler.prepareWrite(filename);
		blockNum = 0;
		//	Send ACK packet (blockNum 0)
		SendDataPacket(new byte[0], blockNum);
		blockNum++;
	}
	

	/*
	 * Method to handle data request
	 * In: block number and data
	 * */
	public void handleData(int block, byte[] fileData) throws IOException {
		
		System.out.print("Data packet received.");
		System.out.print("New block received, writing...");
		if(blockNum == block) {		
			//	Write data to the file, then send the ACK packet
			fileHandler.writeFile(fileData);
			SendDataPacket(new byte[0], blockNum);
			//	Close fileHandler if reached the end
			if(fileData.length == 512) {	
				blockNum++;
			}else {
				fileHandler.close();
			}			
		}else {
			// wrong data packet received 
		}
	}
	
	/*
	 * Method to handle ACK request
	 * In: filename to read
	 * */
	public void handleACK() throws IOException {
		System.out.print("ACK packet Received.");
		SendDataPacket(fileHandler.readFile(), blockNum);
		blockNum++;
	}
	
	/*
	 *	Method to send data packet with block number
	 *	If send ACK packet, leave data null
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
		sendPacket = new DatagramPacket(sendData, sendData.length,
				receivePacket.getAddress(), receivePacket.getPort());

    //	Send packet
		try {
			displaySend(sendPacket);
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	
		//	Close fileHandler if reached the end
		if(data.length < 512) {
			fileHandler.close();
		}
	}
	
	public void handleERROR() {
		System.out.print("ERROR packet Received.");
	}

	/*
	*	Method to terminate program
	*/
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
