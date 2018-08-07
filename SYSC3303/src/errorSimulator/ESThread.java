package errorSimulator;

import java.io.IOException;
import java.net.*;

public class ESThread extends Thread{

	private DatagramPacket receivedPacket, sendPacket;
	private DatagramSocket receiveSendSocket;

	private int errorType, errorChoice, errorPacket, blockChoice, delayChoice;
	private int errorOpcode, errorPacketSize, errorPacketFormat;
	private String errorMode, errorFilename;
	private InetAddress clientAddress;
	private int clientPort;
	private int serverPort = 69;
	private RequestParser rp;
	private int ID;
	private boolean continueListen = true;
	private InetAddress serverAddress;
	private DatagramSocket errorSocket;
	private boolean newSocket = false;
	
	public ESThread(int errorType, int errorChoice, int errorPacket, int blockChoice, int delayChoice, 
			        int errorOp, String errorMode, String errorFilename, int errorPS,
			        int errorPF, DatagramPacket received, InetAddress serverAddress) {

		//		byte[] sendData = new byte[1024];

		//		sendPacket = new DatagramPacket(sendData, sendData.length);

		this.errorType = errorType;
		this.errorChoice = errorChoice;
		this.errorPacket = errorPacket;
		this.blockChoice = blockChoice;
		this.delayChoice = delayChoice;
		this.errorOpcode = errorOp;
		this.errorMode = errorMode;
		this.errorFilename = errorFilename;
		this.errorPacketSize = errorPS;
		this.errorPacketFormat = errorPF;
		this.receivedPacket = received;
		this.clientAddress = receivedPacket.getAddress();
		this.clientPort = receivedPacket.getPort();
		this.serverAddress = serverAddress;

		try {
			receiveSendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		rp = new RequestParser();
		ID = receiveSendSocket.getLocalPort();

		System.out.println("Created a thread");

	}

	public void run() {
		System.out.println("Thread is running...");
		
		if(!ifClient(receivedPacket)) {
			if(serverPort == -1) 
				this.serverPort = receivedPacket.getPort();
		}
		
		System.out.println("Packet received:");
		while(continueListen) {
			tryError(receivedPacket);
			if(newSocket) {
				receive(errorSocket);
			}else {
				receive(receiveSendSocket);
			}
			
		}

	}

	private void tryError(DatagramPacket receivedPacket){
		rp.parseRequest(receivedPacket.getData(), receivedPacket.getLength());

		
		
		if(ifError(receivedPacket)) {
			System.out.println("Target packet received, making error...");
			if(errorType == 1) {
				System.out.println("Making Transimission error...");
				makeTransmissionError(receivedPacket);
				errorType = 0;
			}else if(errorType == 2) {
				System.out.println("Making Error Code error...");
				makeErrorCodeError(receivedPacket);
				errorType = 0;
			}

		}else {	
			transferPacket(receivedPacket,receiveSendSocket);		
		}
	}

	private boolean ifError(DatagramPacket receivedPacket) {
		if(errorType == 0) {
			System.out.println("No Error");
			return false;
		}
		if(errorPacket == rp.getType()) {
			if(errorPacket != 3 && errorPacket != 4) {
				return true;
			}else {
				if(blockChoice == rp.getBlockNum()) {
					return true;
				}
			}
		}
		return false;
	}
  
	public void sendPacket(DatagramPacket sendPacket, DatagramSocket socket) {

		try {
			socket.send(sendPacket);
			System.out.println("Error Simulator: Packet sent:");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	} 

	public void receive(DatagramSocket socket) {
		try {
			socket.setSoTimeout(10000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println("Receving a packet...");
			socket.receive(receivedPacket);
			
			if(!ifClient(receivedPacket)) {
				if(serverPort == 69) 
					this.serverPort = receivedPacket.getPort();
			}

			System.out.println("Error Simulator: Packet received:");
		}catch(SocketTimeoutException e1) {
			System.out.println(ID + ": Timeout, closing thread.");
			continueListen = false;

			return;
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	

	private void makeTransmissionError(DatagramPacket receivedPacket) {
		if(errorChoice == 1) {
			if(rp.getType() == 3 || rp.getType() == 4) {
				System.out.println("Target " + parsePacketName(rp.getType()) + " Packet Block# : " + rp.getBlockNum() + "has lost.");
			}else {
				System.out.println("Target " + parsePacketName(rp.getType()) + " Packet has lost.");
			}
		}
		else if(errorChoice == 2) {
			if(rp.getType() == 3 || rp.getType() == 4) {
				System.out.println("Delaying Target" + parsePacketName(rp.getType()) + " Packet Block# : " + rp.getBlockNum() + " " + delayChoice + "ms");
			}else {
				System.out.println("Delaying Target" + parsePacketName(rp.getType()) + " Packet " + delayChoice + "ms");
			}
			
			try {
				Thread.sleep(delayChoice);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			transferPacket(receivedPacket,receiveSendSocket);
		}else if(errorChoice == 3) {
			if(rp.getType() == 3 || rp.getType() == 4) {
				System.out.println("Duplicating Target" + parsePacketName(rp.getType()) + " Packet Block# : " + rp.getBlockNum());
			}else {
				System.out.println("Duplicating Target" + parsePacketName(rp.getType()) + " Packet");
			}
			
			transferPacket(receivedPacket,receiveSendSocket);
			transferPacket(receivedPacket,receiveSendSocket);
		}else {
			System.out.println("invalid error choice");
		}
	}

	public void transferPacket(DatagramPacket receivedPacket, DatagramSocket socket) {
		System.out.println("Passing packet received...");
		if(ifClient(receivedPacket)) {
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.serverAddress, this.serverPort);
			sendPacket(sendPacket, socket);
		}else {
			sendPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getLength(), this.clientAddress, this.clientPort);
			sendPacket(sendPacket, socket);
		}
	}
	
	public void makeErrorCodeError(DatagramPacket receivedPacket) {
		System.out.println("Parsing error code choice...");
		
		
		if(errorPacket == 1 || errorPacket == 2) {
			switch(errorChoice) {
				case 1: 
					System.out.println("Modify Mode for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyMode(receivedPacket, errorMode), receiveSendSocket); 
					break;
				case 2: 
					System.out.println("Modify Opcode for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyOpcode(receivedPacket, errorOpcode),receiveSendSocket); 
					break;
				case 3: 
					System.out.println("Modify Filename for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyFilename(receivedPacket, errorFilename),receiveSendSocket); 
					break;
				case 4: 
					System.out.println("Modify Packet size for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyPacketSize(receivedPacket, errorPacketSize),receiveSendSocket); 
					break;
				case 5: 
					System.out.println("Modify Packet format for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyPacketFormat(receivedPacket, errorPacketFormat),receiveSendSocket); 
					break;
				case 6: 
					System.out.println("Making Error Code 5 for target" + parsePacketName(rp.getType()) + " Packet ...");
					transferPacket(modifyOpcode(receivedPacket, 3),receiveSendSocket); 
					break;
				default: 
					System.out.println("invalid error choice"); 
					break;
			}

		}else if(errorPacket == 3 || errorPacket == 4) {
			
			switch(errorChoice) {
				case 1: 
					System.out.println("Modify Opcode for target" + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferPacket(modifyOpcode(receivedPacket, errorOpcode),receiveSendSocket);
					break;
				case 2: 
					System.out.println("Modify Block number for target " + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferPacket(modifyBlockNum(receivedPacket),receiveSendSocket);
					break;
				case 3: 
					System.out.println("Modify Packet size for target " + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					transferPacket(modifyPacketSize(receivedPacket, errorPacketSize),receiveSendSocket); 
					break;
				case 4: 
					System.out.println("Making Error Code 5 for target " + parsePacketName(rp.getType()) + "Packet Block# : " + rp.getBlockNum() + "...");
					////error code 5
					try {
						errorSocket = new DatagramSocket();
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					transferPacket(receivedPacket,errorSocket);
					newSocket = true;
					break;
				default: 
					System.out.println("invalid error choice"); 
					break;
			}
		}else {
			System.out.println("Error Packet received, transferring...");
			transferPacket(receivedPacket,receiveSendSocket);
		}
	}
	
	public DatagramPacket modifyOpcode(DatagramPacket receivedPacket, int opcode) {
		byte[] sendData = new byte[receivedPacket.getLength()];
		for(int i = 0; i < receivedPacket.getLength(); i++) {
			sendData[i] = receivedPacket.getData()[i];
		}
		
		sendData[1] = (byte) opcode;
		
		System.out.println("Previous Opcode: 0" + rp.getType() + "    After Change: 0" + opcode);
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyMode(DatagramPacket receivedPacket, String mode) {
		
		
		byte[] errorMode = mode.getBytes();
		byte[] sendData = new byte[4 + rp.getFilename().getBytes().length + errorMode.length];
		System.out.println(rp.getFilename().getBytes().length);
		System.out.println(errorMode.length);
		
		sendData[0] = 0;
		sendData[1] = (byte)rp.getType();
		
		for(int i = 0; i < rp.getFilename().getBytes().length; i++) {
			sendData[2+i] = rp.getFilename().getBytes()[i];
		}
		
		sendData[2+rp.getFilename().getBytes().length] = 0;    //0 1 2 3 4 5 6 7 8 9 10
		
		for(int i = 0; i < errorMode.length; i++) {
			sendData[3+rp.getFilename().getBytes().length + i] = errorMode[i]; 
		}
		
		sendData[3 +rp.getFilename().getBytes().length + errorMode.length] = 0;
		
		System.out.println("Previous Mode: " + rp.getMode() + "    After Change: " + mode);
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyFilename(DatagramPacket receivedPacket, String filename) {
		byte[] fileName = filename.getBytes();
		byte[] sendData = new byte[4 + fileName.length + rp.getMode().getBytes().length];
		
		sendData[0] = 0;
		sendData[1] = (byte)rp.getType();
		
		for(int i = 0; i < fileName.length; i++) {
			sendData[2+i] = fileName[i];
		}
		
		sendData[2+fileName.length] = 0;
		
		for(int i = 0; i < rp.getMode().getBytes().length; i++) {
			sendData[3+fileName.length + i] = rp.getMode().getBytes()[i]; 
		}
		
		sendData[3 + fileName.length + rp.getMode().getBytes().length] = 0;
		
		System.out.println("Previous Filename: " + rp.getFilename() + "    After Change: " + filename);  
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		
		return packet;
	}
	
	public DatagramPacket modifyPacketSize(DatagramPacket receivedPacket, int packetSize) {
		byte[] sendData = new byte[packetSize];
		byte[] data = receivedPacket.getData();
		
		System.arraycopy(data, 0, sendData, 0, packetSize);
		
		System.out.println("Previous Packet Size: " + receivedPacket.getLength() + "    After Change: " + packetSize);
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyPacketFormat(DatagramPacket receivedPacket, int packetFormat) {
		byte[] filename = rp.getFilename().getBytes();
		byte[] mode = rp.getMode().getBytes();
		byte[] sendData = new byte[4+filename.length+mode.length];
		
		sendData[0] = 0;
		sendData[1] = (byte)rp.getType();
		
		for(int i = 0; i < filename.length; i++) {
			sendData[2+i] = filename[i];
		}
		
		sendData[2+filename.length] = (byte)packetFormat;
		
		for(int i = 0; i < mode.length;i++) {
			sendData[3+filename.length + i] = mode[i];
		}
		
		sendData[3+filename.length+mode.length] = (byte) packetFormat;
		System.out.println("Modify the zero padding in RRQ/WRQ from 0 to" + packetFormat);
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}
	
	public DatagramPacket modifyBlockNum(DatagramPacket receivedPacket) {
		
		System.out.println("receivedPacket.getLength(): " + receivedPacket.getLength());
		
		byte[] sendData = new byte[receivedPacket.getLength()-2];
		
		sendData[0] = 0;
		sendData[1] = (byte) rp.getType();
		
		if(errorPacket == 3) {
			for(int i = 0; i <rp.getFileData().length; i++) {
				sendData[2+i]= rp.getFileData()[i];
			}
		}
		
		System.out.println("Delete Block Number for received packet");
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receivedPacket.getAddress(),receivedPacket.getPort());
		return packet;
	}


	public boolean ifClient(DatagramPacket receivedPacket) {
		if(receivedPacket.getPort() == clientPort && receivedPacket.getAddress().equals(clientAddress)) return true;
		return false;
	}

	public String parsePacketName(int packetType) {
		
		String packetName;
		
		if(packetType == 1) {
			packetName = "RRQ";
		}else if(packetType == 2) {
		    packetName = "RRQ";
		}else if(packetType == 3) {
		    packetName = "DATA";
		}else if(packetType == 4) {
			packetName = "ACK";
		}else {
			packetName = "ERROR";
		}
		return packetName;	
	}
}
