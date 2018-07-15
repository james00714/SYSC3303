package ErrorSimulator;

import java.net.*;
import java.io.*;
import java.util.*;

//UI

public class ErrorSimulator{
	
	private int userOption;
	private int operationChoice;
	private ErrorType error;
	private ErrorSController controller;
	
	public ErrorSimulator() {

		error = new ErrorType();   
		controller = new ErrorSController();
		
	}
	
	
	public void ErrorMenu() throws IOException{
		
		Scanner sc = new Scanner(System.in);
		error.printErrorMenu();
	
		boolean valid = false;
		
		while(!valid) {
			
			userOption = sc.nextInt();
			
			if(userOption >= 0 && userOption <= 9) {

				this.controller.distribute(userOption);
				valid = true;
				
			}
			else valid = false;
			
		}
		
	}
	
	
	public void IllegalOperation() throws IOException{
		
		Scanner sc = new Scanner(System.in);
		error.printIllegalOperation();
		operationChoice = sc.nextInt(); 
		//...
		
	}
	
	public static void main(String args[]) throws IOException{
		
		ErrorSimulator i = new ErrorSimulator();
		
		//while(true) {		
			i.ErrorMenu();
		//} 
	}

}