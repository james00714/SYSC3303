/*
 * FileHander class to handle requests that need file operation
 * */

package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {
	
	private FileInputStream fs;
	private FileOutputStream os;
	private File fileToWrite;
	private byte[] fileBuffer;
	
	public FileHandler() {}
	
	/*
	 * Method to handle read request
	 * In: file path or name
	 * */
	public void readFile(String file) throws IOException {

		if(!file.contains("\\")) {
			file = "src\\client\\files\\" + file;
		}	
		//	Try loading the file
		try {
			//System.out.println(file);
			fs = new FileInputStream(file);
            
        }catch (IOException e) {
        	e.printStackTrace();
        }

	}
	
	/*
	 * Method to read the rest of file
	 * In: file path or name
	 * */
	public byte[] readFile() {

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
		}catch (IOException e) {
			e.printStackTrace();
		}	
    
		// return data loaded
		return fileBuffer;
	}
	
	/*
	 * Method to prepare work before write a file
	 * In: file path or name
	 * */
	public void prepareWrite(String file) {
		
		if(file.contains("\\")) {
			
			 int index = 0;
			for(int i = 0; i < file.length(); i++){
				if(file.charAt(i) == '\\'){
					index = i;
				}
			}
			file = file.substring(index, file.length());
			 
			
		}else {
			file = "src\\client\\files\\" + file;
		}
		
		
		try{
			fileToWrite = new File(file);
			System.out.println(file);
			// create if does not exists
			if (!fileToWrite.exists()) {
				fileToWrite.createNewFile();
			}
			// set output stream to write file
			os = new FileOutputStream(fileToWrite);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Method to close stream
	 * In: file data
	 * */
	public void close() throws IOException{
		if(fs != null) {
			fs.close();
		}
		if(os != null){
			os.close();
		}
	}

	/*
	 * Method to prepare write data to a file
	 * In: file data
	 * */
	public boolean writeFile(byte[] fileData) throws IOException {
		System.out.println("Writing file...");
		try{
			os.write(fileData);
			os.flush();	
			
		} catch (IOException e) {
			
			// Access denied
			if(e.getMessage().contains("Access is denied")) {
				System.out.println("ERROR: Access Violation.");
				
			//	Not enough space
			}else if(e.getMessage().equals("There is not enough space on the disk")) {
				System.out.println("ERROR: Not Enough Space.");
			}else {
				System.out.print("IO Exception: likely:");
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
