package client;

import java.io.*;
import java.util.Scanner;

public class Client {
	private	String mode, request, fig;
	private	String fileName;
	private	boolean running = true;


	public Client (){}

	public String getMode (){
		return this.mode;
	}

	public String getRequest (){
		return this.request;
	}

	public String getFileName (){
		return this.fileName;
	}

	public String getFig (){
		return this.fig;
	}


	public void menu (){
		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome to client V2 <Enter quit to quit anytime :(>");
		System.out.println("Please select your mode:");
		System.out.println("1. Normal  <Client, Server>");
		System.out.println("2. Test  <Client, Error Simulator, Server>");
		mode = sc.next();
		if (!mode.equals("quit")) {
			System.out.println("Please select your request");
			System.out.println("1. RRQ <Read Request>");
			System.out.println("2. WRQ <Write Request>");
			request = sc.next();
			if (!request.equals("quit")) {
				System.out.println("Please enter your file Name ");
				fileName = sc.next();
				if(!fileName.equals("quit")) {
					System.out.println("Please enter your mode for data");
					System.out.println("1. Verbose");
					System.out.println("2. Quiet");
					fig = sc.next();		
				}else {
					sc.close();
				}
			}else {
				sc.close();
			}
		}else {
			sc.close();
		}
	}

	public void start (Sender s) throws IOException{

		if (mode.equals("quit") || request.equals("quit") || fileName.equals("quit") || fig.equals("quit")) {
			running = false;
			System.out.println("Thank your for using our program. Goodbye!");
			s.Close();
			//t.close();	
		}else {
			if (mode.equals("1")){
				s.start(this, s, 69);
				s.Receiver();
			}else if(mode.equals("2")){
				s.start(this, s, 23);
				s.Receiver();	
			}else {
				System.out.println("Invalid mode input, please try again.");
			}
		}	
	}

	public static void main(String[] args) throws IOException {
		Client c = new Client();
		Sender n = new Sender (c);
		while(c.running) {
			c.menu();
			c.start(n);	
		}		
	}
}
