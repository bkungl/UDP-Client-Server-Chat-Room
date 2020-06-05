Relevant Files:
	Client.java - the client class (run multiple at a time)
	Server.java - the server class (run 1 at a time)
	ClientConnection.java - each new thread uses this
	Message.java - useful for reliable transfer, ACK message
	UDPFileRecevier.java - receive a file
	UDPFileSender.java - send a file 
	file.txt - example file to upload

—————————————————————————

Compilation Instructions:
	In console type the following:
	java Client.java ClientConnection.java Message.java Server.java UDPFileReceiver.java UDPFileSender.java


—————————————————————————


Running Instructions:
	Open a server file with java Server in terminal

	Then, open multiple clients with java Client in each terminal

	The program is buggy with sending a file then a message, so first try to have a client refreshing for messages and one client sending messages:
		In one client, type rfr
		In another client, type msg example message
		Check the client that is refreshing to see message, can send another message from another client as well.
	
		Then, in another client try putting and getting a file. It is slightly buggy when the file size is bigger than one packet of data.

