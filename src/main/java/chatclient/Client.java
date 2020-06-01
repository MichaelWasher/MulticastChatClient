/**
 *
 * @author  Michael Washer
 * @since   2014
 * 
*/
import java.net.*;
import java.lang.Thread;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;  
import java.lang.IllegalArgumentException;

class Client{
    
    // Properties
    MulticastSocket ms;
    int portNum = 40202;
    InetAddress multicastAddress;
    String messagePrefix = "";
    String username = "";

    ReceiveMessageWorker receiveThread;
    

    String quittingKey = "#quit";
    public Client(String username, InetAddress multicastAddress, int portNum) throws IllegalArgumentException 
    {
        //Set Up Client
        // TODO: perform checks on the variables
        // TODO: Remove 'contstructor' pass around
        try{
            //Set Up Client
            this.multicastAddress = multicastAddress;
            this.portNum = portNum;
            this.ms = new MulticastSocket(portNum);          
            this.ms.joinGroup(this.multicastAddress);
            this.username = username;
            // Set message prefix
            messagePrefix = String.format("%s(%s)\t",this.username, this.multicastAddress.getHostAddress());
        }catch(Exception e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("One of the arguments provided were unable to be processed.");
        }
    }

    public void start()
    {
        try{
            
            // TODO Ouput message so User knows how to leave
            // TODO Keep message at the top of the screen at all times
            
            //Start Looking for Messages to Receive
            this.open();
            //Loop for sending Messages
            Scanner s = new Scanner(System.in);

            while(true)
            {
                String message = s.nextLine().toLowerCase();
                if(message.toLowerCase().equals(quittingKey))
                    break;
                sendMessage(messagePrefix + message);
            }
            //Close all Open Streams and Desconstruct Method
            s.close();
            this.halt();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void open()   {
        ReceiveMessageWorker receiveThread = new ReceiveMessageWorker(ms);
        receiveThread.start();
    }
    public void halt()
    {
        ms.close();
        receiveThread.halt();
    }

    public void sendMessage(String messageContents) throws IOException
    {
        //Create Packet
        DatagramPacket msgPacket = new DatagramPacket(messageContents.getBytes(), messageContents.length(),
             multicastAddress, portNum);
        //Send Packet
        ms.send(msgPacket);
    }
    
    protected class ReceiveMessageWorker extends Thread
    {
        protected boolean loop = true;
        protected MulticastSocket ms;
        protected DateTimeFormatter messageTimeFormatter;
        public ReceiveMessageWorker(MulticastSocket ms)
        {
            this.ms = ms;
        }
        private DatagramPacket receivePacket() throws IOException
        {
            byte[] buf = new byte[1000];
             DatagramPacket recv = new DatagramPacket(buf, buf.length);
             messageTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");  
             ms.receive(recv);    //Blocks Here
             return recv;
        }
        private String buildMessageFromPacket(DatagramPacket packet)
        {
            //Create String
            LocalDateTime now = LocalDateTime.now();
            String outputString = String.format("%s : %s", messageTimeFormatter.format(now), new String(packet.getData(), StandardCharsets.UTF_8));
            return outputString;
        }
        private void halt()
        {
            loop = false;
        }
        public void run()
        {
            try{
                while(loop)
                {
                    DatagramPacket dp = this.receivePacket();
                    String outputString = this.buildMessageFromPacket(dp);
                    // TODO: This output is written where new messages are written
                    System.out.println(outputString);
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
