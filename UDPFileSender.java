/*
* This class is the main workforce of sending a file to the server. 
* It handles the outgoing file and assures its arrival at the server.
*/

import java.io.*;
import java.net.*;

public class UDPFileSender
{
    //the id of the currently sent segment
    private int segmentID;
    //the number of packets that had to be resent
    private int reSendCount;
    //the byte arrays for storing and sending data
    private byte[] msg, buffer;
    //the file reader object
    private FileInputStream fileReader;
    //the UDP socket
    private DatagramSocket datagramSocket;
    //the integer values of the length of file, current position of packet, number of bytes read
    private int fileLength, currentPos, bytesRead;
    //the size of the header
    private final int packetOverhead = 106; // packet overhead

    //constructor
    public UDPFileSender(DatagramSocket socket, DatagramPacket initPacket) throws IOException
    {
        msg = new byte[512];
        buffer = new byte[512];
        datagramSocket = socket;

        //setup DatagramSocket with correct Inetaddress and port of receiver
        datagramSocket.connect(initPacket.getAddress(), initPacket.getPort());
        segmentID = 0;
    }

    //sends the file
    public void sendFile(File theFile) throws IOException
    {
        fileReader = new FileInputStream(theFile);
        fileLength = fileReader.available();

        System.out.println("*** Filename: " + theFile.getName() + " ***");
        System.out.println("*** Bytes to send: " + fileLength + " ***");

    	//send file using UDP socket
        send((theFile.getName() + "::" + fileLength).getBytes());

	//wait for a reply and put in the created packet
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(reply);

        //waits for receiver to indicate OK to send
	//establish TCP-like connection to send
        if (new String(reply.getData(), 0, reply.getLength()).equals("OK"))
        {
            System.out.println("*** Got OK from receiver - sending the file ***");

            //outer while to control when send operation comlete
            //inner while to control ACK messages from receiver
            while (currentPos < fileLength)
            {
                bytesRead = fileReader.read(msg);

		//call message class object
                Message message = new Message(segmentID, msg, bytesRead);
                System.out.println("Sending segment " + message.getSegmentID() + " with " + bytesRead + " byte payload.");
                byte[] test = serialize(message);

		System.out.println("begin send first packet");
                send(test, bytesRead + packetOverhead);
		System.out.println("end send first packet");

                currentPos = currentPos + bytesRead;

                //handle ACK of sent message object, timeout of 2 seconds. If segementID ACK is not received
                //resend segment
                datagramSocket.setSoTimeout(2000);
                boolean receiveACK = false;
                while (!receiveACK)
                {
		    //try to receive packet
                    try
                    {
                        datagramSocket.receive(reply);
                    } catch (SocketTimeoutException e)
                    {
			//if packet not received, resend and increment count
                        send(test, bytesRead + packetOverhead);
                        System.out.println("*** Sending segment " + message.getSegmentID() + " with " + bytesRead + " payload again. ***");
                        reSendCount++;
                    }
		//if segment id is same at packets data, the ACK is received
                    if (new String(reply.getData(), 0, reply.getLength()).equals(Integer.toString(message.getSegmentID())))
                    {
                        System.out.println("Received ACK to segment" + new String(reply.getData(), 0, reply.getLength()));
                        segmentID++;
                        receiveACK = true;
                    }
                }
            }
            System.out.println("*** File transfer complete...");
            System.out.println(reSendCount + " segments had to be resent. ***");
        } else
        {
            System.out.println("Recieved something other than OK... exiting");
        }
    }

    //send a packet with the socket
    private void send(byte[] message, int length) throws IOException
    {
        DatagramPacket packet = new DatagramPacket(message, length);
        datagramSocket.send(packet);
    }

    //send a packet with the socket
    private void send(byte[] message) throws IOException
    {
        DatagramPacket packet = new DatagramPacket(message, message.length);
        datagramSocket.send(packet);
    }

    //serialize is for returning a byte array given a writable object
    public byte[] serialize(Object obj) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(obj);
        objectStream.flush();
        return byteStream.toByteArray();
    }
}