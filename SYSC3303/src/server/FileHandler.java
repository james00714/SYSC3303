/*
 * FileHander class to handle requests that need file operation
 * */

package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {

	private static String directory = "src\\server\\files\\";
	
	private FileInputStream fs;
	private FileOutputStream os;
	private byte[] fileBuffer;
	private RequestHandler RH;
	private File f;

	public FileHandler(RequestHandler requestHandler) {
		RH = requestHandler;
	}

	/*
	 * Method to handle read request
	 * @param	file	file path or name
	 * @return	byte[]	file data
	 * */
	public byte[] readFile(String file) throws IOException {

		if(!file.contains("\\")) {
			file = directory + file;
		}	
		
		//	Try loading the file
		int count;
		f = new File(file);
		if(!f.exists()) {
			System.out.println("ERROR: File Not Found.");
			RH.SendErrorPacket(1, "File not found");
			return null;
		}
		try {
			fs = new FileInputStream(file);
			fileBuffer = new byte[512];	
			if ((count = fs.read(fileBuffer)) != -1){			
				//	If reached the end of file, size will be reduced
				if(count < fileBuffer.length) {
					byte[] tempBuffer = new byte[count];
					for(int i = 0; i < count; i++) {
						tempBuffer[i] = fileBuffer[i];
					}
					fileBuffer = tempBuffer;
				}
			}
			if(count == -1) return new byte[0];
		}catch(IOException e) {
			close();
			// Access denied
			if(e.getMessage().contains("Access is denied")) {
				System.out.println("ERROR: Access Violation.");
				RH.SendErrorPacket(2, "Access violation");
			}else {
				System.out.print("IO Exception: likely:");
				e.printStackTrace();
			}
			return null;
		}
		

		// return data loaded
		return fileBuffer;
	}

	/*
	 * Method to read the rest of file
	 * In: file path or name
	 * */
	public byte[] readFile(){

		if(!f.canRead()) {
			System.out.println("ERROR: Access Violation.");
			RH.SendErrorPacket(2, "Access violation");
			return null; 
		}
		
		int count;
		fileBuffer = new byte[512];
		
		try {
			if ((count = fs.read(fileBuffer)) != -1){
				//	If reached the end of file, size will be reduced
				if(count < fileBuffer.length) {
					byte[] tempBuffer = new byte[count];
					for(int i = 0; i < count; i++) {
						tempBuffer[i] = fileBuffer[i];
					}
					fileBuffer = tempBuffer;
				}
			}
			if(count == -1) return new byte[0];
		} catch (IOException e) {
			close();
			// Access denied
			if(e.getMessage().contains("Access is denied")) {
				System.out.println("ERROR: Access Violation.");
				RH.SendErrorPacket(2, "Access violation");
			}else {
				System.out.print("IO Exception: likely:");
				e.printStackTrace();
			}
			return null;
		}		
		// return data loaded
		return fileBuffer;
	}

	/*
	 * Method to prepare work before write a file
	 * In: file path or name
	 * */
	public boolean prepareWrite(String file) {

		System.out.println("Prepare Writing File: " + file);
		
		File dir = new File(directory);
		long space = dir.getUsableSpace();
		System.out.println(directory + ", available space: " + space);
		
		if(space <= 0) {
			System.out.println("ERROR: Not Enough Space.");
			RH.SendErrorPacket(3, "Disk full or allocation exceeded");
			return false;
		}
		
		if(file.contains("\\")) {
			int index = 0;
			for(int i = 0; i < file.length(); i++){
				if(file.charAt(i) == '\\'){
					index = i;
				}
			}
			file = file.substring(index, file.length());
		}
		file = directory + file;
		f = new File(file);
		
		// File already exist
		if(f.exists()) {
			System.out.println("ERROR: File already exists.");
			RH.SendErrorPacket(6, "File already exists");
			return false;
		}
		try{
			f.createNewFile();
			// set output stream to write file
			os = new FileOutputStream(f);		
		} catch (IOException e) {
			
			// Access denied
			if(e.getMessage().contains("Access is denied")) {
				System.out.println("ERROR: Access Violation.");
				RH.SendErrorPacket(2, "Access violation");
			}else {
				System.out.print("IO Exception: likely:");
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	/*
	 * Method to close stream
	 * In: file data
	 * */
	public void close(){
		try {
			if(fs != null) {
				fs.close();
			}
			if(os != null){
				os.close();
			}
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			e.printStackTrace();
		}
	}

	/*
	 * Method to prepare write data to a file
	 * In: file data
	 * */
	public boolean writeFile(byte[] fileData){
		
		System.out.println("Writing file...");
		
		if(!f.canWrite()) {
			System.out.println("ERROR: Access Violation.");
			RH.SendErrorPacket(2, "Access violation");
			return false;
		}
		
		try{
			os.write(fileData);
			os.flush();	
			
		} catch (IOException e) {
			close();
			// Access denied
			if(e.getMessage().contains("Access is denied")) {
				System.out.println("ERROR: Access Violation.");
				RH.SendErrorPacket(2, "Access violation");
				
			//	Not enough space
			}else if(e.getMessage().equals("There is not enough space on the disk")) {
				System.out.println("ERROR: Not Enough Space.");
				RH.SendErrorPacket(3, "Disk full or allocation exceeded");
			}else {
				System.out.print("IO Exception: likely:");
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	public static void setDir(String dir) {
		if(dir.charAt(dir.length()-1) != '\\')
			dir += '\\';
		directory = dir;
	}
	
	public static void setDefaultDir() {
		directory = "src\\server\\files\\";
	}
}
