import java.io.*;
import java.net.*;
import java.util.*;

public class ErrorSimulator{
  private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
  private DatagramPacket receivePacket, sendPacket;
  private int port = 3303; //Proxy port 3303
  private int clientPort;  //use to store client port
  private InetAddress clientAddress; //use to store client address

  public ErrorSimulator(){
    try{
      sendSocket = new DatagramSocket();
      receiveSocket = new DatagramSocket(port);
      sendReceiveSocket = new DatagramSocket();
    }catch(SocketException se){
      se.printStackTrace();
      System.exit(1);
    }
  }

  public void sendAndReceive(){
    byte data[] = new byte[20];
    receivePacket = new DatagramPacket(data,data.length);

    System.out.println("Proxy: Waiting for Packet from client.\n");

    // Block until a datagram packet is received from receiveSocket.
    try {
       receiveSocket.receive(receivePacket);
    } catch (IOException e) {
       System.out.print("IO Exception: likely:");
       System.out.println("Receive Socket Timed Out.\n" + e);
       e.printStackTrace();
       receiveSocket.close();
       sendSocket.close();
       sendReceiveSocket.close();
       System.exit(1);
    }

    clientAddress = receivePacket.getAddress();
    clientPort = receivePacket.getPort();

    // Process the received datagram.
    System.out.println("Intermediate Host: Packet received from client:");
    System.out.println("From host: " + receivePacket.getAddress());
    System.out.println("host port: " + receivePacket.getPort());
    int len = receivePacket.getLength();
    System.out.println("Length: " + len);
    System.out.print("Containing: " );

    System.out.println(new String(receivePacket.getData()));
    System.out.println(receivePacket.getData() + "\n");

    int serverPort1 = 3004;
    int serverPort2 = 3804;
    Random random = new Random();
    int randomPort = random.nextBoolean() ? serverPort1 : serverPort2;

    String received = new String(receivePacket.getData(), 0, receivePacket.getLength());

    try{
      sendPacket = new DatagramPacket(received.getBytes(), receivePacket.getLength(), receivePacket.getAddress(), randomPort);
      sendReceiveSocket.send(sendPacket);
    }catch(UnknownHostException e){
      e.printStackTrace();
      receiveSocket.close();
      sendSocket.close();
      sendReceiveSocket.close();
      System.exit(1);
    }catch(IOException e){
      e.printStackTrace();
      receiveSocket.close();
      sendSocket.close();
      sendReceiveSocket.close();
      System.exit(1);
    }

    System.out.println("Proxy: Sending packet to Server:");
    System.out.println("To host: " + sendPacket.getAddress());
    System.out.println("Destination host port: " + sendPacket.getPort());
    len = sendPacket.getLength();
    System.out.println("Length: " + len);
    System.out.print("Containing: ");
    System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"

    System.out.println("Intermediate Host: packet sent");
    System.out.println("Proxy: Waiting for Packet from Server.\n");

    byte  serverResponse[] = new byte[20];
	  receivePacket = new DatagramPacket(serverResponse, serverResponse.length);

	  try {
	   sendReceiveSocket.receive(receivePacket);
	  }catch(IOException e) {
	    	e.printStackTrace();
	    	receiveSocket.close();
        sendSocket.close();
			  sendReceiveSocket.close();
	      System.exit(1);
	  }

    System.out.println("Proxy: Packet received from Server");
    System.out.println("From host: " + receivePacket.getAddress());
    System.out.println("host port: " + receivePacket.getPort());
    System.out.print("Containing: ");
    System.out.println(Arrays.toString(receivePacket.getData()));
    // Form a String from the byte array.
    System.out.println(new String(receivePacket.getData()));
    System.out.println("");

    sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),clientAddress,clientPort);

    System.out.println( "Proxy: Sending packet to Client:");
    System.out.println("To host: " + sendPacket.getAddress());
    System.out.println("Destination host port: " + sendPacket.getPort());
    len = sendPacket.getLength();
    System.out.println("Length: " + len);
    System.out.print("Containing: ");
    System.out.println(new String(sendPacket.getData(),0,len));

    try{
      sendSocket.send(sendPacket);
    }catch(IOException e){
      e.printStackTrace();
      receiveSocket.close();
      sendSocket.close();
      sendReceiveSocket.close();
      System.exit(1);
    }
    System.out.println("Host: Packet sent.");
  }

  public void connect(){
    int i = 0;
    while(i<100){
      sendAndReceive();
      i++;
    }
    receiveSocket.close();
    sendSocket.close();
    sendReceiveSocket.close();
  }

  public static void main(String args[]){
    ErrorSimulator i = new ErrorSimulator();
    i.connect();
  }
}
