/*
 * FileHander class to handle requests that need file operation
 * */

package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {
	
	private FileInputStream fs;
	private FileOutputStream os;
	private File fileToWrite;
	private String path = System.getProperty("user.dir") + "\\src\\Server";  // default path
	private byte[] fileBuffer;
	
	public FileHandler() {}
	
	/*
	 * Method to handle read request
	 * In: file path or name
	 * */
	public byte[] readFile(String file) throws IOException {

		if(!file.contains("\\")) {
			file = path + "\\" + file;
		}
		
		//	Try loading the file
		try {
			int count;
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
        }catch (IOException e) {
        	e.printStackTrace();
        }
    
		// return data loaded
		return fileBuffer;
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

		if(!file.contains("\\")) {
			file = path + "\\" + file;
		}
		try{
			fileToWrite = new File(file);
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
	public void writeFile(byte[] fileData) throws IOException {
		os.write(fileData);
		os.flush();
	}
}
