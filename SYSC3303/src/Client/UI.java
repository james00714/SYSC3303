import java.io.*;
import java.util.Scanner;

public class UI {
	private static String mode;
	private static String fileName;
	private static String request;
	public UI(){
		this.mode = "";
		this.fileName = "";
		this.request = "";	
	}
	
	
	public String getMode () {
		return this.mode;
	}
	
	public String getFileName () {
		return this.fileName;
	}
	
	public String getRequest () {
		return this.request;
	}
	
	public void setMode (String mode){
		this.mode = mode;
	}
	
	public void setFileName (String fileName){
		this.fileName = fileName;
	}
	
	public void setRequest (String request){
		this.request = request;
	}

	public static void IT1 (){
		Scanner sc = new Scanner (System.in);
		System.out.println ("Welcome to this program. This program transfer a file ");
		System.out.println ("Please select your mode <Quiet/Normal/Verbose/Test>");		
		mode = sc.next();
		System.out.println("Please enter your request <RRQ/WRQ>");
		request = sc.next();
		System.out.println("Please enter the file name :)\ns");
		fileName = sc.next();
	}


}
