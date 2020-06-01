/**
 * 
 * This application is a CLI tool for 
 * decentralised chat using Multicast communication.
 * I have commented this code to hopefully provide future
 * developers with an understanding of how Multicast-IP
 * communications can be achieved in Java.
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

class ChatClient{
	public static void main(String[] args)
	{
		// TODO: Provide usage
		// TODO: Allow input IP address + Naiive args check
		// TODO: Allow input of portNum... Throw if x < 1024
		// TODO: Allow users to provide a username within the chat system.
		// NOTE: Don't check if it's used.

		Client client = new Client();
		client.start();
	}
}



class Client{
	
	MulticastSocket ms;
	int portNum = 40202;
	String addressString = "239.0.202.1"; ///Change Later
	InetAddress multicastAddress;
	ReceiveMessages receiveThread;

	String quittingKey = "#quit";
	public Client()
	{
		//Set Up Client
		// TODO:  Remove 'contstructor' pass around
		constructor();
	}

	private void constructor()
	{
		try{
			//Set Up Client
			multicastAddress = InetAddress.getByName(addressString);
			ms = new MulticastSocket(portNum);
			ms.joinGroup(multicastAddress);	
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void start()
	{
		if(multicastAddress == null || ms == null)
		{
			constructor();
		}
		try{
			
			// TODO Ouput message so User knows how to leave
			// TODO Keep message at the top of the screen at all times

			//Start Looking for Messages to Receive
			ReceiveMessages receiveThread = new ReceiveMessages(ms);
			receiveThread.start();
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
			halt();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
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


// TODO: Add basic tests