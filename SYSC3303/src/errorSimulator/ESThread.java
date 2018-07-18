/*
 * Thread class for error simulator
*/
package errorSimulator;

import java.io.*;
import java.net.*;

public class ESThread extends Thread{

	private DatagramSocket sendReceiveSocket;	
	private DatagramPacket receiveServerPacket, receiveClientPacket, sendPacket;

	private InetAddress clientAddr; //serverAddr; for multi-pc
	private int clientPort, serverPort;

	public ESThread(DatagramPacket received) {

		byte[] data1 = new byte[1024];
		byte[] data2 = new byte[1024];

		receiveServerPacket = new DatagramPacket(data1, data1.length);
		receiveClientPacket = new DatagramPacket(data2, data2.length);

		clientAddr = received.getAddress();
		clientPort = received.getPort();
		//serverAddr = null;
		serverPort = -1;

		this.receiveClientPacket = received;

		try{
			sendReceiveSocket = new DatagramSocket();
		}catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}

	}

	public void run() {

		receiveFromClient();

	}

	/*
	 * Modify the packet by given choice in the UI
	*/
	public byte[] Modify(DatagramPacket packet, RequestParser RP, UI ui) {
		int choice = ui.mainMenu();
		byte[] sendData = null;
		if(choice == 0) {
			sendData = new byte[packet.getLength()];
			for(int i = 0; i < packet.getLength();i++) {
				sendData[i] = packet.getData()[i];
			}
			//send(sendData, port);
		}else if(choice == 1) {
			int newOp = ui.askOpCode();
			sendData = new byte[packet.getLength()];
			for(int i = 0; i < packet.getLength();i++) {
				sendData[i] = packet.getData()[i];
			}
			sendData[1] = (byte) newOp;
			//send(sendData, port);		
		}else if(choice == 2) {
			int second = ui.type2();
			if (second == 1) {
				byte [] fileName = ui.askFileName();
				sendData = new byte[4 + fileName.length];
				sendData[0] = 0;
				sendData[1] = 1;
				for (int i = 0; i < fileName.length; i++) {
					sendData[2+i] = fileName[i];
				}
				sendData[3+fileName.length] = 0;

			}else if (second == 2) {
				sendData = new byte[packet.getLength()];
				for(int i = 0; i < packet.getLength();i++) {
					sendData[i] = packet.getData()[i];
				}
				int Bk = ui.askBkNumber();
				sendData[2] = (byte) (Bk / 256);
				sendData[3] = (byte) (Bk % 256);

			}else if (second == 3) {
				sendData = new byte[packet.getLength()];
				for(int i = 0; i < packet.getLength();i++) {
					sendData[i] = packet.getData()[i];
				}
				int code = ui.askErrCode();
				sendData[1] = (byte) code;
			}else {
				System.out.println("Choice 2 Error.");
			}

		}else if(choice == 3) {
			int third = ui.type3();
			if (third == 1) {
				byte [] data = ui.askData();
				sendData = new byte[4+data.length];
				for(int i = 0; i < 4;i++) {
					sendData[i] = packet.getData()[i];
				}
				
				for (int j = 0; j < data.length; j++) {
					sendData[4 + j] = data[j];
				}
				
			}else if (third == 2) {
				byte [] msg = ui.askErrMSG();
				sendData = new byte[4+msg.length];
				for(int i = 0; i < 4 ;i++) {
					sendData[i] = packet.getData()[i];
				}
				
				for (int j = 0; j < msg.length; j++) {
					sendData[4+j] = msg[j];
				}
					sendData[4+msg.length] = 0;
			}else {
				System.out.println("Choice 3 Error");
			}
		}else {
			System.out.println("Error");
		}

		return sendData;
	}

	/*
	 * Send the packet
	*/
	public void send(byte[] data, InetAddress addr, int port) {

		sendPacket = new DatagramPacket(data, data.length,
				addr, port);

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		displaySend(sendPacket);	
	}

	/*
	 * Receive packet from client
	*/
	public void receiveFromClient() {
		if(receiveClientPacket == null) {
			byte data[] = new byte[1024];
			receiveClientPacket = new DatagramPacket(data, data.length);
			try {
				System.out.println("ES: Waiting for Client...");
				sendReceiveSocket.receive(receiveClientPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}	
		RequestParser RP = new RequestParser();
		RP.parseRequest(receiveClientPacket.getData(), receiveClientPacket.getLength());
		UI myUI = new UI(RP);
		////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////
		if(serverPort == -1) {
			send(Modify(receiveClientPacket, RP, myUI),clientAddr, 69);
		}else {
			send(Modify(receiveClientPacket, RP, myUI),clientAddr, serverPort);
		}
		////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////
		receiveFromServer();
	}

	/*
	 * Receive packet from server
	*/
	public void receiveFromServer() {
		try {
			System.out.println("ES: Waiting for server response...");
			sendReceiveSocket.receive(receiveServerPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if(serverPort == -1) {
			serverPort = receiveServerPacket.getPort();
			//serverAddr = receiveServerPacket.getAddress();
		}
		RequestParser RP = new RequestParser();
		RP.parseRequest(receiveServerPacket.getData(), receiveServerPacket.getLength());
		UI myUI = new UI(RP);
		send(Modify(receiveServerPacket, RP, myUI), clientAddr, clientPort);
		receiveClientPacket = null;
		receiveFromClient();
	}

	/*
	 * Print send packet
	*/
	public void displaySend(DatagramPacket sendPacket) {
		System.out.println("ES : Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
	}

}
