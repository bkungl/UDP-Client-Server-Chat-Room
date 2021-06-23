import java.io.*;
import java.net.*;
import java.util.HashSet;
/*
* Server class, only one instance running and connects to multiple clients, multiple
* threads
*/
public class ServerImproved
{
	//id of client
    private static int clientID = 1;
    //udp socket
    private static DatagramSocket serverSocket;

    //hash set of ports
    private static HashSet<Integer> portSet = new HashSet<Integer>();


    public static void main(String[] args) throws IOException
    {
        System.out.println("Server started.");
        byte[] buffer = new byte[512];

        /**
         * multi-threaded, 1 thread per connection (only put/get)
         */
        serverSocket = new DatagramSocket(8550);
        while (true)
        {
            try
            {
                DatagramPacket packet =  new DatagramPacket(buffer, buffer.length );
                serverSocket.receive(packet);

		System.out.println("adding " + packet.getPort() + " to hashes");
		portSet.add(packet.getPort());

		//if its a message just send it to the rest of the users
		String tempStrMsgCheck = new String(packet.getData(), 0, packet.getLength());
		if(tempStrMsgCheck.substring(0,3).equals("msg")) {
			
			// Extract the message from the packet and make it into a string, then trim off any end characters
			//String clientMessage = (new String(receivePacket.getData())).trim();
 			

			//get rest of packet that isn’t “msg”
			tempStrMsgCheck = tempStrMsgCheck.substring(4);
			
			// Print some status messages
			//System.out.println("Client Connected - Socket Address: " + receivePacket.getSocketAddress());
			//System.out.println("Client message: \"" + clientMessage + "\"");          
 
			// Get the IP address and the the port number which the received connection came from
			InetAddress clientIP = packet.getAddress();           
 
			// Print out status message
			//System.out.println("Client IP Address & Hostname: " + clientIP + ", " + clientIP.getHostName() + "\n");
 
			// Get the port number which the recieved connection came from
			//int clientport = receivePacket.getPort();
			//System.out.println("Adding "+clientport);
			//portSet.add(clientport);
 
			// Response message			
			//String returnMessage = clientMessage.toUpperCase();          
			System.out.println(tempStrMsgCheck);
			// Create an empty buffer/array of bytes to send back 
			byte[] sendData  = new byte[1024];
 
			// Assign the message to the send buffer
			sendData = tempStrMsgCheck.getBytes();
			
			for(Integer port : portSet) 
			{
				//System.out.println(port != 8550);//8550 is servers port
				if(port != 8550) 
				{
					// Create a DatagramPacket to send, using the buffer, the clients IP address, and the clients port
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, port); 
					//System.out.println("Sending");
					// Send the echoed message          
					serverSocket.send(sendPacket);    
				}
			}


		}
		else{

                	System.out.println("SERVER: Accepted connection.");
        	        System.out.println("SERVER: received"+new String(packet.getData(), 0, packet.getLength()));
			System.out.println("packet indexing: " + packet.getPort());

		

                	//new socket created with random port for thread
        	        DatagramSocket threadSocket = new DatagramSocket();

			//make new threads with client connection (that implements runnable)	
	                Thread t = new Thread(new ClientConnection(threadSocket, packet, clientID++));	

                	t.start();
		}

            } catch (Exception e)
            {
                System.err.println("Error in connection attempt.");
            }
        }
    }

    public HashSet<Integer> getPorts(){
	return this.portSet;
    }
}