import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *Client connection class, the thread system, each client has a connection to the server
 * 
 */
public class ClientConnection implements Runnable
{
	//id of client
    private int clientID;
    //byte array to hold buffer
    private byte[] buffer;
    //number of bytes to receive
    private int bytesToReceive;
    //UDP socket
    private DatagramSocket clientSocket;
    // udp packets initialization
    private DatagramPacket packet, initPacket;
    //strings
    private String userInput, filename, initString;

    //constructor to set up socket and packet
    public ClientConnection(DatagramSocket clientSocket, DatagramPacket packet, int clientID) throws IOException
    {
        this.clientSocket = clientSocket;
        this.packet = packet;
        this.clientID = clientID;
    }

    //run the connection
    @Override
    public void run()
    {
        try
        {
            //512 for data remainder for overhead
            buffer = new byte[618];

            System.out.println("THREAD: " + new String(packet.getData(), 0, packet.getLength()));

            initString = new String(packet.getData(), 0, packet.getLength());
		
            StringTokenizer t = new StringTokenizer(initString);
	
	    System.out.println(initString);

            userInput = t.nextToken();//but is chill with this
            filename = t.nextToken();//doesnt like this
            if (t.hasMoreTokens())
            {
                bytesToReceive = new Integer(t.nextToken()).intValue();
            }

	    //determine action and act accordingly
            switch (messageType.valueOf(userInput))
            {
                case put:
                    //sends a message gets the new port information to the client
                    send(packet.getAddress(), packet.getPort(), ("OK").getBytes());

                    //create Object to handle incoming file
                    new UDPFileReceiver(clientSocket);

                    break;
                case get:
                    File theFile = new File(filename);

                    send(packet.getAddress(), packet.getPort(), ("OK").getBytes());

                    //create object to handle out going file
                    UDPFileSender fileHandler = new UDPFileSender(clientSocket, packet);
                    fileHandler.sendFile(theFile);
                    break;
		case msg:
		    send(packet.getAddress(), packet.getPort(), ("OK").getBytes());
			
		    //create object to handle incoming message
		    new UDPFileReceiver(clientSocket, 0);

   		    break;

                default:
                    System.out.println("Incorrect command received.");
                    break;
            }
        } catch (IOException ex)
        {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("*** Transfer for client " + clientID + " complete. ***");
    }

    //send message
    private void send(InetAddress recv, int port, byte[] message) throws IOException
    {
        DatagramPacket packet = new DatagramPacket(message, message.length, recv, port);
        clientSocket.send(packet);
    }

    //send message
    private void send(byte[] message) throws IOException
    {
        DatagramPacket packet = new DatagramPacket(message, message.length);
        clientSocket.send(packet);
    }

    //enumeration for types, rfr is not included because that is not relevant
    public enum messageType
    {
        get, put, msg;
    }
}