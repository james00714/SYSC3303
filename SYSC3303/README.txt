SYSC3303 Iteration 4  Team 11

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
     
User Instruction (Error Simulator, Please select the error user want to simulate 
in the errorSimulator(UI) first then go to client console(UI) to start select ):
---------------------------------------

  Error Main Menu:
		
		----------Error Selection----------
		     0. Normal Operation
		     1. Transmission Error
		     2. Error Codes (4 or 5)                              <----------------------- Error Code 4, 5 temporily not work now, still fixing the code 
		     >>>>>>>> input quit to exit this program

-------------------------------------------------------------------------
After selecting error type:

    		If user select 0, error simulator start working without simulating any error

		If user select 1, Transmission Error will show

		(User cannot select 2 temporily)

		If user input quit, the error simulator's listener will stop

		If user do invalid input, the UI will show "Invalid input, please try again." and go to the next loop to start a new error main menu.

-------------------------------------------------------------------------
After selecting 1 in error main menu (Show Transmission Error):

		---------- Transmission Error ----------
		    1. Lose a packet
		    2. Delay a packet
		    3. Duplicate a packet
		    4. Back to Error main menu
		>>>>>>>> input quit to exit this program

(User choose an error to simulate)
-------------------------------------------------------------------------
After selecting transmission error:

    		If user select 1, go to packet selection menu

		If user select 2, go to packet selection menu

		If user select 3, go to packet selection menu

		If user select 4, and go to the next loop to start a new error main menu.

		If user input quit, the error simulator's listener will stop

		If user do invalid input, the UI will show "Invalid input, please try again." and show tansmission error menu again.

-------------------------------------------------------------------------
Show the packet selection menu:

		---------- Packet Selection ----------
		    1. RRQ
		    2. WRQ
		    3. DATA
		    4. ACK
		    5. ERROR                                   <-------- Error packet not handle yet
		    6. Back to Error Menu
		>>>>>>>> input quit to exit this program


(User choose an packet to simulate the error)
-------------------------------------------------------------------------
After selecting packet:

    		If user select 1 and transmission error is "Delay a packet", then go to delay selection menu.

		If user select 2 and transmission error is "Delay a packet", then go to delay selection menu.

		If user select 3, go to block selection menu to decide the block# of packet to simulate error, 
		then if transmission error is "Delay a packet", then go to delay selection menu.

		If user select 4, go to block selection menu to decide the block# of packet to simulate error, 
		then if transmission error is "Delay a packet", then go to delay selection menu.

		(User cannot select 5 temporily)

		If user input quit, the error simulator's listener will stop

		If user do invalid input, the UI will show "Invalid input, please try again." and show packet menu again.

--------------------------------------------------------------------------------------------------------------------
Show Delay Selection Menu:			  |Show Block Selection Menu:
                        			  |
   ---------- Delay Selection ----------	  |	---------- Block Selection ----------
    Please enter delay time (ms)...		  |     Please enter block number...
    Enter -1 to go back to Error Menu		  |     Enter -1 to go back to Error Menu
>>>>>>>> input quit to exit this program	  |>>>>>>>> input quit to exit this program
						  |
(User input number to decide delay time)	  |(User choose a block of ACK/DATA packet to simulate the error)
						  |
						  |
---------------------------------------------------------------------------------------------------------------------
After finish all selection, error simulator start listening and the next loop for Error main menu start.
User can change error Selection to modify the error for simulating at any time.

(Since the error simulator will print the packet information when request transfer, the new error menu will be hard to find.
To change the error selection, just input 0(Normal Operation), 1(Transmission Error), 2(Error code Error) or quit and the next
menu will show)
---------------------------------------------------------------------------------------------------------------------


User Instruction (Client Side):
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
		
		In Client UI, for RRQ, input filename s (If s is already exist in /src/client/files, delete this file)
			      for WRQ input filename s (If s is already exist in /src/server/files, delete this file)

		(Timeout 3000ms Up tp 4 times, then disconnect)
                (Error Simulator thread closes if no packet is received in 15 seconds)

		Lost a packet:
			
			RRQ: packet lost no resend, reprompt request from user after time out

			WRQ: packet lost no resend, reprompt request from user after time out

			ACK: ACK lost no resend. Wait for DATA packet, disconnect after times out

			DATA: resend DATA packet, Wait for ACK packet, disconnect after times out 

		Delay a packet:

			RRQ: reprompt request from user after time out
			
			WRQ: reprompt request from user after time out

			ACK (blk# = n): resend DATA packet (blk# = n), send new ACK packet (blk# = n), 
				  	receive old ACK packet(blk# = n), send DATA packet (blk# = n+1), receive
					new ACK packet (blk# = n), wait for ACK packet (blk# = n+1)

			DATA (blk# = n): resend a new DATA packet (blk# = n), receive new DATA packet (blk# = n),
					 receive old DATA packet (blk# = n), send ACK packet(blk# = n), wait for DATA packet (blk# = n+1)

		Duplicate a packet:

			RRQ: send DATA packet(blk# = 1),send ACK packet(blk# = 1)

			WRQ: send ACK packet(blk# = 0),send DATA packet(blk# = 1)

			ACK (blk# = n-1): Ignore duplicate ACK packet (blk# = n-1), send DATA packet (blk# = n-1)

			DATA(blk# = n): Ignore duplicate DATA packet, send ACK packet(blk# = n)

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
