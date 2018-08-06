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
	
	/*
	 * Static method to add a client instance to the client list
	 * @param 	c	client to add.
	 * @return		operation result
	 * */
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
	
	/*
	 * Static method to remove a client instance from the client list
	 * @param 	c	client to remove.
	 * */
	public static void removeFromClients(Client c) {
		synchronized(activeClients){
			activeClients.remove(c);
			activeClients.notifyAll();
		}
	}
	
	/*
	 * Static method to check if all connections have finished when shut down server
	 * The method will wait if some connections are still running
	 * */
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
	
	/*
	 * Static method to check if a client that has same information already exists
	 * @param	client	client instance to check
	 * @return			result
	 * */
	public static boolean findSameClient(Client client) {
		for(Client c : activeClients) {
			if(c.getAddress().equals(client.getAddress()) && 
					c.getPort() == client.getPort()) return true;
		}
		return false;
	}
	
	/*
	 * Static method that returns number of active connections
	 * @param	client	client instance to check
	 * @return			result
	 * */
	public static int getClientsSize() {
		return activeClients.size();
	}
	
	/*
	 * @return		address of client
	 * */
	public InetAddress getAddress() {
		return myAddress;
	}
	
	/*
	 * @return		port number of client
	 * */
	public int getPort() {
		return myPort;
	}
	
	/*
	 * @return		current block number of client
	 * */
	public int getBlockNum() {
		return myBlockNum;
	}
	
	/*
	 * Method to increase current block number
	 * */
	public void incrementBlockNum() {
		myBlockNum++;
	}
	
	/*
	 * @return		FileHandler
	 * */
	public FileHandler getFileHandler() {
		return myFH;
	}
	
	/*
	 * Method to clean up current client information
	 * 	Close I/O stream
	 * 	Remove from client list
	 * */
	public void close() throws IOException {
		myFH.close();
		removeFromClients(this);
	}
}
