/*
 * Prepare and send the packet after UI in the client class
 */
package client;

import java.io.*;
import java.net.*;

public class Sender {
	private Client c;
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket receivePacket, sendPacket;
	private FileHandler fileHandler;
	private RequestParser RP;
	private int blockNumber = 0, port, finalBlock, TID;
	private String filename;
	private static final int TIMEOUTMAX = 4;

	public Sender(Client c){
		this.c = c;
		RP = new RequestParser();
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {   
			se.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Receive packet
	 */
	public void Receiver () throws IOException{
		System.out.println("Client: waiting a packet...");
		byte data[] = new byte[1024];
		receivePacket = new DatagramPacket(data, data.length);

		int end = 0;
		sendReceiveSocket.setSoTimeout(3000);


		try {
			sendReceiveSocket.receive(receivePacket);
			TID = receivePacket.getPort();

		} catch(SocketTimeoutException e) {
			//e.printStackTrace();
			//System.exit(1);
			end++;
			System.out.println("Timeout "+ end + " time(s)");

			if(end == TIMEOUTMAX) {
				System.out.println("ERROR: No Response From Client "+TID+" , closing transmission now.");
				return;
			}
			Receiver();
		}

		PrintReceiver(receivePacket);
		ReceiveHandler(receivePacket);
	}

	/*
	 * Handle received packet and go to corresponding handle function
	 */
	public void ReceiveHandler (DatagramPacket receivePacket) throws IOException{
		RP.parseRequest(receivePacket.getData(), receivePacket.getLength());

		if (receivePacket.getPort() != TID) {
			System.out.println("Unknown TID error");
			SendErrorPacket(4, "Unknown transfer ID");
			Receiver();	
		}else {
			switch (RP.getType()) {
			case 3:	DATA(receivePacket);
			break;
			case 4:	ACK(receivePacket);
			break;
			case 5:	ERR();
			break;
			}
		}
	}

	/*
	 * Deal with received DATA packet, check the block number 
	 */
	public void DATA (DatagramPacket receivePacket) throws IOException{
		int resend = 0;
		System.out.println("Received Data packet.");
		byte [] send = new byte [4];
		int blockNum = RP.getBlockNum();
		int length = receivePacket.getLength();

		if (blockNum == blockNumber){
			if(blockNum == 1) {
				fileHandler = new FileHandler();
				fileHandler.prepareWrite(filename);
			}
			if(length > 516){		
				System.out.println("Wrong packet received (oversized).");
				Receiver();
			}else {
				if(fileHandler.writeFile(RP.getFileData()) == false) {
					SendErrorPacket(3, "Disk full or allocation exceeded");
				}else {
					send[0] = 0;
					send[1] = 4;
					send[2] = (byte)(blockNumber/256);
					send[3] = (byte)(blockNumber%256);
					SendPacket (send);
					blockNumber++;	
					if (length == 516){			
						Receiver();
					}else if(length < 516){
						System.out.println("Transfer Complete");
						fileHandler.close();
					}
				}
			}
			//check delay, duplicate by bk number
		}else if (blockNum < blockNumber) {
			//Re-try 4 times
			if (resend < 4) {
				System.out.println("packet bk number "+blockNum+" VS current bk number "+ blockNumber+"");
				System.out.println("Packet already received (duplicated), Resending ACK packet");
				Resend(blockNumber++);
				resend++;
			}else {
				System.out.println("Resending");
			}
		}else{
			System.out.println("Error bk number, lololololololol");
		}
	}


	/*
	 * Deal with received ACK packet
	 */
	public void ACK (DatagramPacket receivePacket) throws IOException{
		System.out.println("Received ACK packet.");
		byte [] send; 
		int blockNum = RP.getBlockNum();

		if (blockNum == blockNumber){
			if(finalBlock == blockNum) {
				System.out.println("Transfer Complete");
				fileHandler.close();
			}else {
				byte[] fileData = fileHandler.readFile();
				int length = fileData.length;
				blockNumber++;
				send = new byte [4+ length];
				send[0] = 0;
				send[1] = 3;
				send[2] = (byte)(blockNumber/256);
				send[3] = (byte)(blockNumber%256);
				for (int i = 0; i < fileData.length; i++){
					send[4+i] = fileData[i];
				}
				SendPacket(send);
				if (fileData.length < 512){	
					finalBlock = blockNumber;
				}
				Receiver();
			}
		}else if (blockNum < blockNumber) {
			System.out.println("Duplicate ACK packet received (delayed)");
			System.out.println("Good bye Sorcerer's Apprentice Bug :heart");
			SendErrorPacket(4, "Illegal TFTP operation");
		}else{
			System.out.println("Error, Invalid block received");
			System.out.println("packet bk number "+blockNum+" VS current bk number "+ blockNumber+"");
			SendErrorPacket(4, "Illegal TFTP operation");
		}	
	}

	/*
	 * Deal with received error packet
	 */
	public void ERR (){
		System.out.println("Received Error Packet.");
		System.out.println("Error code: " + RP.getErrorCode());
		System.out.println("Error Message: " + RP.getErrorMsg());
	}

	//resend ack packet
	public void Resend (int BKNumber) {
		byte [] send = new byte [4];
		send[0] = 0;
		send[1] = 4;
		send[2] = (byte)(BKNumber/256);
		send[3] = (byte)(BKNumber%256);
		SendPacket (send);
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
				receivePacket.getAddress(), receivePacket.getPort());

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
	 * Send packets
	 */
	public void SendPacket(byte [] packet) {
		//		System.out.println("Client: sending a packet...");
		if(receivePacket == null) {
			try {
				sendPacket = new DatagramPacket(packet, packet.length,
						InetAddress.getLocalHost(), port);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}else {
			sendPacket = new DatagramPacket(packet, packet.length,
					receivePacket.getAddress(), receivePacket.getPort());
		}

		/*
		try {
			Thread.sleep(500);
		}catch(InterruptedException e){}
		 */
		//		Send packet
		try {
			// displaySend(sendPacket);
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		PrintSender(sendPacket);
	}

	/*
	 * Handle user request from UI
	 */
	public void  RequestHandler(String request, String fileName) throws IOException{
		byte [] send = null;
		byte [] length = fileName.getBytes();
		filename = fileName;
		String mode = "netascii";
		byte [] length2 = mode.getBytes();

		send = new byte [4 +length.length + length2.length];
		send[0] = 0;
		if(request.equals("1")) {
			send[1] = 1;
			//fileHandler = new FileHandler();
			//fileHandler.prepareWrite(fileName);
			blockNumber = 1;
			System.out.println("Read request generated.");
		}else {
			send[1] = 2;
			fileHandler = new FileHandler();
			fileHandler.readFile(filename);
			blockNumber = 0;
			finalBlock = -1;
			System.out.println("Write request generated.");
		}

		for (int i = 0; i < length.length; i++){
			send[2+i] = length[i];
		}

		send[2+length.length] = 0;

		for (int j = 0; j<length2.length; j++) {
			send[3+length.length+j] = length2[j];
		}

		send[3+length.length + length2.length] = 0;

		System.out.println("printing out byte array");
		for (int i = 0; i < send.length; i++) {
			System.out.println(send[i]);
		}
		SendPacket (send);
	}


	public void PrintReceiver (DatagramPacket receivePacket){
		Verbose v = new Verbose();
		Quiet q = new Quiet();

		if (c.getFig().equals("1")) {
			v.PrintReceiverV(receivePacket);
		}
		else {
			q.PrintReceiverQ(receivePacket);
		}	
	}

	/*
	 * Print sended packet
	 */
	public void PrintSender (DatagramPacket sendPacket) {
		Verbose v = new Verbose();
		Quiet q = new Quiet();

		if (c.getFig().equals("1")) {
			v.PrintSender(sendPacket);
		}
		else {
			q.PrintSenderQ(sendPacket);
		}
	}

	/*
	 * Close the sockeet
	 */
	public void Close (){
		sendReceiveSocket.close();
	}

	/*
	 * Start the client
	 */
	public void start (Client c, int portNum) throws IOException{
		System.out.println("Normal mode selected");
		receivePacket = null;

		this.port = portNum;
		this.RequestHandler(c.getRequest(), c.getFileName());
	}

}
