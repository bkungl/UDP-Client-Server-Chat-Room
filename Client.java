import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * Client class, each client run this and get put, get a file or send a message or wait to receive messages
 * current limitation is that the 
 */
public class Client
{

    //byte array for buffer
    private static byte[] buffer;
    //port
    private static int port = 8550;
    //udp socket
    private static DatagramSocket socket;
    //bufferedreader for input from console
    private static BufferedReader stdin;
    //select an action from input
    private static StringTokenizer userInput;
    //init UDP packets
    private static DatagramPacket initPacket, packet;

    public static void main(String[] args) throws IOException
    {
        socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");

        buffer = new byte[618];

        stdin = new BufferedReader(new InputStreamReader(System.in));
	
	//determines if a blank packet should be sent to establish connection (NOT GET/PUT)
	boolean blankSent = false;
	boolean running = true;

	//while loop to keep client running after an action (allows messages to be received)
	while(running){

        	String selectedAction = selectAction();
	        userInput = new StringTokenizer(selectedAction);
	
        	try
        	{
	            switch (messageType.valueOf(userInput.nextToken()))
        	    {
			//upload a file to server
                	case put:
	                    packet = new DatagramPacket((selectedAction).getBytes(), (selectedAction).getBytes().length, address, port);
        	            socket.send(packet);

                	    File theFile = new File(userInput.nextToken());

	                    initPacket = receivePacket();

        	            //create object to handle out going file
                	    UDPFileSender fileHandler = new UDPFileSender(socket, initPacket);
                    	    fileHandler.sendFile(theFile);
	                    break;


        	        case get:
                	    packet = new DatagramPacket((selectedAction).getBytes(), (selectedAction).getBytes().length, address, 8550);
	                    socket.send(packet);

        	            initPacket = receivePacket();

                	    socket.send(initPacket);

	                    //create Object to handle incoming file
        	            new UDPFileReceiver(socket);
                	    break;

			case msg:
			    //send a blank message to add the users information to server
			    if(blankSent!=true){
				byte[] data = new byte[400];
	        		data = "msg*".getBytes();
        
				DatagramPacket blankPacket = new DatagramPacket(data, data.length, address, port);
        			socket.send(blankPacket);
				System.out.println("finished send blank");
				blankSent = true;
			    }
      			
			
			    packet = new DatagramPacket((selectedAction).getBytes(), (selectedAction).getBytes().length, address, port);

	                    //create object to handle out going message
			
			    //now send packet to server
			    socket.send(packet);

			    //wait to receive
			    //if 0 there duplicate, flush buffer
			    /*if(tempCount==0){
				socket.receive(packet);
				tempCount++;	
			    }*/
			    socket.receive(packet);

			    //Display the message from server
			    System.out.println(new String(packet.getData(), 0, packet.getLength()));
				
			    break;

			//wait to receive messages from other clients
			case rfr:

			    //send a blank message to add the users information to server
			    if(blankSent!=true){
				byte[] data = new byte[400];
        			data = "msg*".getBytes();
        
				DatagramPacket blankPacket = new DatagramPacket(data, data.length, address, port);
	        		socket.send(blankPacket);
				System.out.println("finished send blank");
				blankSent = true;
			    }		

			    // Create a byte buffer/array for the receive Datagram packet
        		    byte[] receiveData = new byte[1024];
 
	       		    while (true) {            
        	    		if (false)//figure out a way to cancel out later
            			break;
 
            			// Set up a DatagramPacket to receive the data into
		                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
           
        	    	        // Receive a packet from the server (blocks until the packets are received)
                		socket.receive(receivePacket);

	                	// Extract the reply from the DatagramPacket      
	        	        String serverReply =  new String(receivePacket.getData(), 0, receivePacket.getLength());
 
        	        	// print to the screen
				if(!serverReply.equals("")){
		        	        System.out.println("Incoming Message: \"" + serverReply + "\"\n");    
	        		}
			    }
		    
        	    }
 	       } catch (Exception e)
        	{
	            System.err.println("not valid input");
        	}

	        //socket.close();
		}
    }
    //receive a packet from socket
    private static DatagramPacket receivePacket() throws IOException
    {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        return packet;
    }

    //print options and get input
    public static String selectAction() throws IOException
    {
        System.out.println("COMMANDS: get *filename*");
        System.out.println("\t  put *filename*");
	System.out.println("\t msg *message to send*");
	System.out.println("\t rfr to update message box");
	System.out.println("\t  example: put data.txt");
        System.out.print("ftp> ");

        return stdin.readLine();
    }

    public enum messageType
    {
        get, put, msg, rfr;
    }
}