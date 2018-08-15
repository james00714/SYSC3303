/*
j * Prepare and send the packet after UI in the client class
 */
package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Sender {
	private Client c;
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket receivePacket, sendPacket;
	private FileHandler fileHandler;
	private RequestParser RP;
	private int blockNumber = 0, port, finalBlock, TID = -1, end = 0;
	private String filename, currentRequest;
	private boolean continueListen = true;
	private static final int TIMEOUTMAX = 4;
	private Scanner sc = new Scanner(System.in);

	public Sender(Client c){
		this.c = c;
		RP = new RequestParser();
	}

	public void newPort () {
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
		String re;
		boolean resend = true;
		if (c.getFig() == "1"){
			System.out.println("Client: waiting a packet...");
		}
		byte data[] = new byte[1024];
		receivePacket = new DatagramPacket(data, data.length);
		sendReceiveSocket.setSoTimeout(3000);

		try {
			sendReceiveSocket.receive(receivePacket);
			if (receivePacket.getPort() == -1){
				return;
			}
			end = 0;
			PrintReceiver(receivePacket);
		} catch(SocketTimeoutException e) {
			end++;
			System.out.println("Timeout "+ end + " time(s)");
			resend = true;
			if (sendPacket.getData()[1] == 1 || sendPacket.getData()[1] == 2){
				System.out.println("Do you want to Resend? <y/n>");
				re = sc.next();
				if (re.equals("n")){
					resend = false;
				}
				
			}
			
			if(end == TIMEOUTMAX || resend == false) {
				end = 0;
				System.out.println("ERROR: No Response From Server, closing transmission.");
				continueListen = false;
				return;
			}
		
			if (sendPacket.getData()[1] == 1 ){
				System.out.println("Resending RRQ");
				//	System.out.println(RequestParser.parseBlockNum(sendPacket.getData()[2], sendPacket.getData()[3]));
				sendReceiveSocket.send(sendPacket);
			}else if ( sendPacket.getData()[1] == 2 ){
				System.out.println("Resending WRQ");
				//	System.out.println(RequestParser.parseBlockNum(sendPacket.getData()[2], sendPacket.getData()[3]));
				sendReceiveSocket.send(sendPacket);
			}

			else{
				//resend data
				if(currentRequest.equals("2") && sendPacket.getData()[1] == 3 ) {
					System.out.println("Resending...");
					sendReceiveSocket.send(sendPacket);	
				}else {
					System.out.println("Waiting...");
				}
			}
			Receiver ();
		}	
	}

	/*
	 * Handle received packet and go to corresponding handle function
	 */
	public void ReceiveHandler (DatagramPacket receivePacket) throws IOException{
		RP.parseRequest(receivePacket.getData(), receivePacket.getLength());

		if(RP.ifCorrect()){
			if(TID == -1)
				TID = receivePacket.getPort();
			if (receivePacket.getPort() != TID && receivePacket.getPort() != -1) {
				System.out.println("Unknown TID error");
				SendErrorPacket(5, "Unknown transfer ID");
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
		}else{
			if(receivePacket.getPort() != -1){
				System.out.println("Error. Wrong packet type.");
				SendErrorPacket(4, "Illegal TFTP Operation");
			}
			continueListen = false;
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
		
		if (RP.getType() == 3 && blockNumber == 0){
			blockNumber = 1;
		}
		
		if (blockNum == blockNumber){
			if(blockNum == 1) {
				fileHandler = new FileHandler(c);
				fileHandler.prepareWrite(filename);
			}
			if(length > 516){		
				System.out.println("Wrong packet received (oversized).");
			}else {			
				if(fileHandler.writeFile(RP.getFileData()) == false) {
					SendErrorPacket(3, "Disk full or allocation exceeded");
					fileHandler.close();
					continueListen = false;
				}else {
					if (!InOperation(filename)) {
						System.out.println("Access Violation");
						SendErrorPacket(2, "Access Violation");
						fileHandler.close();
						continueListen = false;
					}
					else {
						send[0] = 0;
						send[1] = 4;
						send[2] = (byte)(blockNumber/256);
						send[3] = (byte)(blockNumber%256);
						SendPacket (send);
						blockNumber++;	
						if(length < 516){
							System.out.println("Transfer Complete");
							fileHandler.close();
							continueListen = false;
						}
					}
				}
			}
			//check delay, duplicate by bk number
		}else if (blockNum < blockNumber) {
			//Re-try 4 times
			if (resend < 4) {
				System.out.println("packet bk number "+blockNum+" VS current bk number "+ blockNumber+"");
				System.out.println("Packet already received (duplicated), Resending ACK packet");
				ResendACK(blockNumber-1);
				resend++;
			}
			return;
		}else{
			System.out.println("Error bk number.");	
		}	
	}

	public boolean InOperation (String fileName) {
		File f = new File (c.getLocation() +"\\"+ fileName);
		if (f.canWrite()) {
			return true;
		}

		return false;
	}


	/*
	 * Deal with received ACK packet
	 */
	public void ACK (DatagramPacket receivePacket) throws IOException{
		if (c.getFig() == "1"){
			System.out.println("Received ACK packet.");

		}		
		byte [] send; 
		int blockNum = RP.getBlockNum();
		if (blockNum == blockNumber){
			
			if(finalBlock == blockNum) {
				System.out.println("Transfer Complete");
				fileHandler.close();
				continueListen = false;
			}else {
				if(blockNum == 65535) {
					System.out.println("Error, Reached the file size limit (Block 65535).");
					SendErrorPacket(0, "Reached the file size limit (Block 65535)");
					continueListen = false;
					return;
				}
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
				if (fileData.length < 512){	
					finalBlock = blockNumber;
				}
				SendPacket(send);
			}
		}else if (blockNum < blockNumber) {
			System.out.println("Error, ignoring invalid block received");
			System.out.println("Duplicate ACK packet received (duplicated)");
		}else{
			System.out.println("Error, ignoring invalid block received");
			System.out.println("packet bk number "+blockNum+" VS current bk number "+ blockNumber+"");
		}	
	}

	/*
	 * Deal with received error packet
	 */
	public void ERR () throws IOException{
		System.out.println("Received Error Packet.");
		System.out.println("Error code: " + RP.getErrorCode());
		System.out.println("Error Message: " + RP.getErrorMsg());
		if(fileHandler != null)
			fileHandler.close();
		continueListen = false;
	}

	// send ACK packet
	public void ResendACK (int BKNumber) {
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
	public void SendErrorPacket(int errorCode, String msg) throws IOException{
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
		
		/*
		if (errorCode != 5){
			continueListen = false;
			fileHandler.close();
			return;
		}*/
		
	}

	/*
	 * Send packets

	 */
	public void SendPacket(byte [] packet) {
		//		System.out.println("Client: sending a packet...");
		InetAddress ipAddress = c.getIpAddress();
		if(receivePacket == null) {
			sendPacket = new DatagramPacket(packet, packet.length,
					ipAddress, port);
		}else {
			sendPacket = new DatagramPacket(packet, packet.length,
					receivePacket.getAddress(), receivePacket.getPort());
		}

		
		
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
			fileHandler = new FileHandler(c);
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
		SendPacket (send);
	}


	public void PrintReceiver (DatagramPacket receivePacket){
		Verbose v = new Verbose();

		if (c.getFig().equals("1")) {
			v.PrintReceiverV(receivePacket);
		}
	}

	/*
	 * Print sent packet
	 */
	public void PrintSender (DatagramPacket sendPacket) {
		Verbose v = new Verbose();
		if (c.getFig().equals("1")) {
			v.PrintSender(sendPacket);
		}
	}

	/*
	 * Close the socket
	 */
	public void Close (){
		if(sendReceiveSocket != null)
			sendReceiveSocket.close();
		try {
			fileHandler.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Start the client
	 */
	public void start (Client c, int portNum) throws IOException{
		//	System.out.println("Normal mode selected");
		receivePacket = null;
		TID = -1;
		newPort();
		this.port = portNum;
		currentRequest = c.getRequest();
		RequestHandler(currentRequest, c.getFileName());
		while(continueListen) {
			Receiver();
			ReceiveHandler(receivePacket);
		}
		continueListen = true;
	}

}
