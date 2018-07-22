SYSC3303 Iteration 2  Team 11

	Zhong, Runhe	101016659
	Lau, James	101009129
        Li, Yinan	101012609
	Liu, Meitong	101014282


The goal of this iteration is to assume that errors can occur in the TFTP packets received, so TFTP ERROR packets dealing with
this (Error Code 4, 5) must be prepared, transmitted, received, and handled. For this part, assume that no File I/O errors occur.


Set Up Instruction:
---------------------------------------
      1. extract .zip file
      2. open Eclipse and set workspace path
      3. run Server.java, ErrorSimulator.java and Client.java
      4. go to the console page of Client
      5. type 1 for Normal mode (please enter valid numbers)
      6. type "testWRQ.txt" (for sending testWRQ.txt file to Server / "testRRQ.txt" for sending testRRQ.txt to Client)
      7. type "quit" to exit the program


Description:
---------------------------------------

	Client: request WRD or RRQ
	Server: handle WRQ or RRQ

	ErrorSimulator: modify and form packets, please select which kind of error you would like to form


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

