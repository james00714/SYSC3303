import java.io.*;
import java.net.*;
import java.util.*;

public class Client{
  private DatagramSocket sendReceiveSocket;
  private DatagramPacket sendPacket, receivePacket;

  public Client(){
    try{
      sendReceiveSocket = new DatagramSocket();
    }catch(SocketException se){
      se.printStackTrace();
      System.exit(1);
    }
  }

  public void sendAndReceive(String s){
    byte msg[] = s.getBytes();

    try{
      sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 3303);
    }catch (UnknownHostException e) {
       e.printStackTrace();
       System.exit(1);
    }

    System.out.println("Client: Sending packet:");
    System.out.println("To host: " + sendPacket.getAddress());
    System.out.println("Destination host port: " + sendPacket.getPort());
    int len = sendPacket.getLength();
    System.out.println("Length: " + len);
    System.out.print("Containing: ");
    System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"

    // Send the datagram packet to the server via the send/receive socket.

    try {
       sendReceiveSocket.send(sendPacket);
    } catch (IOException e) {
       e.printStackTrace();
       sendReceiveSocket.close();
       System.exit(1);
    }

    System.out.println("Client: Packet sent to proxy.\n");
    System.out.println("Waiting...");

    byte data[] = new byte[20];
    receivePacket = new DatagramPacket(data, data.length);

    try {
       // Block until a datagram is received via sendReceiveSocket.
       sendReceiveSocket.receive(receivePacket);
    } catch(IOException e) {
       e.printStackTrace();
       sendReceiveSocket.close();
       System.exit(1);
    }

    // Process the received datagram.
    System.out.println("Client: Packet received:");
    System.out.println("From host: " + receivePacket.getAddress());
    System.out.println("Host port: " + receivePacket.getPort());
    len = receivePacket.getLength();
    System.out.println("Length: " + len);
    System.out.print("Containing: ");

    // Form a String from the byte array.
    String received = new String(data,0,len);
    System.out.println(received);
  }

  public void clientStart(){
    String generatedString;
    for(int i = 0; i < 100; i++){
      System.out.println(i+1+".");
      generatedString = stringGenerator();
      sendAndReceive(generatedString);
    }

    System.out.println("All message has been sent!");
    sendReceiveSocket.close();
    System.exit(1);
  }

  public String stringGenerator(){
    String characters = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); //9
    int len = characters.length();

    String result = new String();
    Random random = new Random();

    for (int i=0; i<20; i++)
      result = result + characters.charAt(random.nextInt(len));

    System.out.println(result);

    return result;
  }

  public static void main(String[] args){
      Client c = new Client();
      c.clientStart();
  }
}
