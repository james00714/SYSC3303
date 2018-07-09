import java.io.*;
import java.net.*;
import java.util.*;

public class Server{
  private DatagramSocket receiveSocket, sendSocket;
  private DatagramPacket receivePacket, sendPacket;
  private int port = 3004;

  public Server(){
    try{
      sendSocket = new DatagramSocket();
      receiveSocket = new DatagramSocket(port);
    }catch(SocketException se){
      se.printStackTrace();
      System.exit(1);
    }
  }

  public void receiveAndEcho(){
    byte data[]  = new byte[20];
    receivePacket = new DatagramPacket(data, data.length);
    System.out.println("Server: Waiting for Packet from Proxy.\n");

    try {
       receiveSocket.receive(receivePacket);
    } catch (IOException e) {
       System.out.print("IO Exception: likely:");
       System.out.println("Receive Socket Timed Out.\n" + e);
       e.printStackTrace();
       receiveSocket.close();
       sendSocket.close();
       System.exit(1);
    }

    System.out.println("Server: Packet received from Proxy:");
    System.out.println("From host: " + receivePacket.getAddress());
    System.out.println("Host port: " + receivePacket.getPort());
    System.out.print("Containing: " );

    String received = new String(data,0,receivePacket.getLength());
    System.out.println(received);
    System.out.println(receivePacket.getData() + "\n");

    String msgSend = eliminateVowel(received);

    byte response[] = msgSend.getBytes();
    sendPacket = new DatagramPacket(response, response.length,
              receivePacket.getAddress(), receivePacket.getPort());

    System.out.println("***Server: Sending packet to Host***");
    System.out.println("To host: " + sendPacket.getAddress());
    System.out.println("Destination host port: " + sendPacket.getPort());;
    System.out.print("Containing: ");
    System.out.println(Arrays.toString(response));
    //System.out.println(new String(sendPacket.getData(),0, sendPacket.getLength()));
    System.out.println(sendPacket.getData() + "\n");
    System.out.println("Server: Packet sent.");

    // Send the datagram packet to the client via the send socket.
    try {
      sendSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
      receiveSocket.close();
      sendSocket.close();
      System.exit(1);
    }
  }

  public String eliminateVowel(String s){
    String newMsg = s.replaceAll("[AaEeIiOoUu]","");
    return newMsg;
  }

  public void connect(){
    int i = 0;
    while(i<100){
      receiveAndEcho();
      i++;
    }
    sendSocket.close();
    receiveSocket.close();
    System.exit(1);
  }

  public static void main(String[] args){
    Server s1 = new Server();
    s1.connect();
  }
}
