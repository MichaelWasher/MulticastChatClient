import java.net.*;
import java.lang.Thread;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.io.*;

class Client{
    
    // Properties
	MulticastSocket ms;
	int portNum = 40202;
    InetAddress multicastAddress;
    
	ReceiveMessages receiveThread;

	String quittingKey = "#quit";
    public Client(InetAddress multicastAddress, int portNum) throws IllegalArgumentException 
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
		}catch(Exception e)
		{
			e.printStackTrace();
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
				sendMessage(message);
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
        ReceiveMessages receiveThread = new ReceiveMessages(ms);
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
	
	// TODO: Name more relevant	
	protected class ReceiveMessages extends Thread
	{
		protected boolean loop = true;
		protected MulticastSocket ms;

		public ReceiveMessages(MulticastSocket ms)
		{
			this.ms = ms;
		}
        private DatagramPacket checkMessage() throws IOException
        {
        	byte[] buf = new byte[1000];
 			DatagramPacket recv = new DatagramPacket(buf, buf.length);
 			ms.receive(recv);	//Blocks Here
 			return recv;
        }
        private String processPacket(DatagramPacket packet)
        {
        	//Get Packet Data and String Address
        	byte[] packetData = packet.getData();
        	InetAddress originAddress = packet.getAddress();
        	//Create String
        	String outputString = String.format("%s : %s", originAddress.getHostAddress(), (new String(packetData, StandardCharsets.UTF_8)));
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
	        		DatagramPacket dp = this.checkMessage();
	        		String outputString = this.processPacket(dp);
	        		System.out.println(outputString);
	        	}
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
        }
	}

}
