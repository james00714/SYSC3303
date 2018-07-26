SYSC3303 Iteration 3  Team 11

	Zhong, Runhe	101016659
	Lau, James	101009129
        Li, Yinan	101012609
	Liu, Meitong	101014282


The goal of this iteration is to assume that errors can occur in the TFTP packets received, so TFTP ERROR packets dealing with
this (Error Code 4, 5) must be prepared, transmitted, received, and handled. For this part, assume that no File I/O errors occur.


Description:
---------------------------------------

	Client: request WRD or RRQ
	Server: handle WRQ or RRQ

	ErrorSimulator: modify and form packets, please select which kind of error you would like to form


Set Up Instruction:
---------------------------------------
      1. extract .zip file
      2. open Eclipse and set workspace path
      3. run Server.java and Client.java (No ErrorSimulator.java)
      4. go to the console page of Client and start testing the program
     


User Instruction:s
---------------------------------------
      
      Menu:
		Welcome to client V2 <Enter quit to quit anytime :(>
		Please select your mode:
		1. Normal  <Client, Server>
		2. Test  <Client, Error Simulator, Server>

-------------------------------------------------------------------------
After selecting mode:
		
		Please select your request
		1. RRQ <Read Request>
		2. WRQ <Write Request>

-------------------------------------------------------------------------

After selecting request type:

		Please enter your file Name

Now, users type the filename they want to write or read
and test starts

-------------------------------------------------------------------------
If disk is full:

		Disk full can't read.
		Please delete file and come again :)

-------------------------------------------------------------------------

If the file that users want to read exist in Client:
		
		File already exist error
		Please enter a new file Name

Now, users type the filename they want to read

-------------------------------------------------------------------------

If the file that users want to write not exist in Client:
		
		File not found error
		Please re-enter your file Name

Now, users type the filename they want to write

-------------------------------------------------------------------------

If the file that users want to write to Server is read-only

		Access violation error.
		Please enter a new file Name

Now, users type the filename they want to write

-------------------------------------------------------------------------

If there is no problems in filename:
		
		Please enter your mode for data
		1. Verbose
		2. Quiet
		
-------------------------------------------------------------------------

If users choose Normal mode, program start loop until user type quit
or error is triggered.

-------------------------------------------------------------------------

Test Instructions:

		FileNotFound error:
			Test client side:
				1. Select normal mode
				2. Select WRQ
				3. Enter a file that does not exist in src/client/files
				
			Test server side:
				1. Select normal mode
				2. Select RRQ
				3. Enter a file that does not exist in src/server/files

		FileAlreadyExist error:
			Test client side:
				1. Select normal mode
				2. Select RRQ
				3. Enter "testClientFAE"

			Test server side:
				1. Select normal mode
				2. Select WRQ
				3. Enter "testServerFAE"

		AccessViolation error:
			Test client side:
				1. Select normal mode
				2. Select WRQ
				3. Enter "testClientAV" (Locked)

			Test server side:
				1. Select normal mode
				2. Select RRQ
				3. Enter "testServerAV" (Locked)
				
				To lock the file, right click and select "Properties" -> "Security" -> "Edit" -> "Deny Full Control" for current user.

		
		DiskFullOrAllocationExceeded:
			Run the program on a USB with no free space
			Test client side:
				0. Run Client on the usb drive
				1. Select normal mode
				2. Select RRQ
				3. Enter "testF"

			Test server side:
				0. Delete "testF" file in src/server/files
				1. Run Server on the usb drive
				2. Select normal mode
				3. Select WRQ
				4. Enter "testF"



Responsibilities:
---------------------------------------
- Runhe Zhong : 
      Group leader.
      Assigned tasks to group members
      Code review
      Collaborated on Error Simulator
      Drew UML diagram
      Wrote README.txt

- James Lau :
      Worked on the Client side
      
- Yinan Li :
      Worked on the Server side

- Meitong Liu :
      Collaborated on Error Simulator
      Drew UML and Timing diagrams
      Wrote README.txt
