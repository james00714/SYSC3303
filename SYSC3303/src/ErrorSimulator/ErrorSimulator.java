package ErrorSimulator;



import java.io.*;

import java.net.*;

import java.util.*;



public class ErrorSimulator{

  private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;

  private DatagramPacket receivePacket, sendPacket;

  private int port = 23; //Error Simulator port 23

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

    byte data[] = new byte[1024];

    receivePacket = new DatagramPacket(data,data.length);



    System.out.println("Error Simulator: Waiting for Packet from client.\n");



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

    System.out.println("Error Simulator: Packet received from client:");

    System.out.println("From host: " + receivePacket.getAddress());

    System.out.println("host port: " + receivePacket.getPort());

    int len = receivePacket.getLength();

    System.out.println("Length: " + len);

    printMessage(receivePacket.getData(),len);

   

    String received = new String(receivePacket.getData(), 0, receivePacket.getLength());



    try{

      sendPacket = new DatagramPacket(received.getBytes(), receivePacket.getLength(), receivePacket.getAddress(), 69); 

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



    System.out.println("Error Simulator: Sending packet to Server:");

    System.out.println("To host: " + sendPacket.getAddress());

    System.out.println("Destination host port: " + sendPacket.getPort());

    len = sendPacket.getLength();

    System.out.println("Length: " + len);

    printMessage(sendPacket.getData(),len);

    

    byte[] temp =  sendPacket.getData();

    if(temp[1]==6) {

    	System.out.println("Quit");

    	receiveSocket.close();

        sendSocket.close();

        sendReceiveSocket.close();

    	System.exit(0);

    }



    byte  serverResponse[] = new byte[516];

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



    System.out.println("Error Simulator: Packet received from Server");

    System.out.println("From host: " + receivePacket.getAddress());

    System.out.println("host port: " + receivePacket.getPort());

    len = receivePacket.getLength();

    System.out.println("Length: " + len);

    printMessage(receivePacket.getData(),len);



    sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),clientAddress,clientPort);



    System.out.println( "Error Simulator: Sending packet to Client:");

    System.out.println("To host: " + sendPacket.getAddress());

    System.out.println("Destination host port: " + sendPacket.getPort());

    len = sendPacket.getLength();

    System.out.println("Length: " + len);

    printMessage(sendPacket.getData(),len);



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

  

  public void printMessage(byte[] data,int len) {

	  RequestParser rp = new RequestParser();

	  

	  rp.parseRequest(data,len);

	    

	    if(rp.getType()==1) {

	    	System.out.println("***Parse Read Request***");

	    	System.out.print("Containing: " );

	    	System.out.print("filename: " + rp.getFilename());

	    //	System.out.println("mode: " + rp.getMode());

	    }else if(rp.getType()==2) {

	    	System.out.println("***Parse Write Request***");

	    	System.out.print("Containing: " );

	    	System.out.println("filename: " + rp.getFilename());

	    	//System.out.println("mode: " + rp.getMode());

	    }else if(rp.getType()==3) {

	    	System.out.println("***Parse Data***");

	    	System.out.print("Containing: ");

	    	System.out.println(rp.getBlockNum());

	    	System.out.println(rp.getFileData());

	    }else if(rp.getType()==4) {

	    	System.out.println("***Parse ACK***");

	    	System.out.print("Containing: ");

	    	System.out.print(rp.getBlockNum());

	    }

  }





  public static void main(String args[]){

    ErrorSimulator i = new ErrorSimulator();

    while(true) {

    	  i.sendAndReceive();

    } 

  }

}