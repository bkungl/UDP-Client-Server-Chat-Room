import java.io.Serializable;

/*
* Message class is used in sending a response to the client
* These are the ACK messages
*/
public class Message implements Serializable {

    //segment Id int
    private int segmentID;
    //byte array for the packet
    private byte[] packet;
    //the count of bytes to write out
    private int bytesToWrite;

    public Message(){}

    //constructor
    public Message(int segmentID, byte[] packet, int bytesToWrite) {
        this.segmentID = segmentID;
        this.packet = packet;
        this.bytesToWrite = bytesToWrite;
    }

    public int getBytesToWrite() {
        return bytesToWrite;
    }

    public int getSegmentID() {
        return segmentID;
    }

    public byte[] getPacket() {
        return packet;
    }
}