/*
 * Client class to store information of a valid connection
 * */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Client {
	
	private static List<Client> activeClients = new ArrayList<>();
	
	private InetAddress myAddress;
	private int myPort, myBlockNum;
	private FileHandler myFH;
	
	
	//	Construct with connect information
	public Client(DatagramPacket receivePacket, int blockNum, FileHandler FH) {
		myAddress = receivePacket.getAddress();
		myPort = receivePacket.getPort();
		myBlockNum = blockNum;
		myFH = FH;
	}
	
	public static boolean addToClients(Client c) {
		synchronized(activeClients){
			if(findSameClient(c) == false) {
				activeClients.add(c);
				return true;
			}else {
				return false;
			}
		}	
	}
	
	
	public static void removeFromClients(Client c) {
		synchronized(activeClients){
			activeClients.remove(c);
			activeClients.notifyAll();
		}
	}
	
	public static void closeAll() {
		synchronized(activeClients){
			while(activeClients.size() != 0) {
				try {
					activeClients.wait();
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("All connections finished.");
		}
	}
	
	public static boolean findSameClient(Client client) {
		for(Client c : activeClients) {
			if(c.getAddress().equals(client.getAddress()) && 
					c.getPort() == client.getPort()) return true;
		}
		return false;
	}
	
	
	public static int getClientsSize() {
		return activeClients.size();
	}
	
	/*
	 * Public information getters
	 * */
	public InetAddress getAddress() {
		return myAddress;
	}
	
	public int getPort() {
		return myPort;
	}
	
	public int getBlockNum() {
		return myBlockNum;
	}
	
	public void incrementBlockNum() {
		myBlockNum++;
	}
	
	public FileHandler getFileHandler() {
		return myFH;
	}
	
	//	Cleaner
	public void close() throws IOException {
		myFH.close();
		removeFromClients(this);
	}
}
