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

// TODO: Add basic tests