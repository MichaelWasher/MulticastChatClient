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
import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;
import java.net.InetAddress;
import java.net.UnknownHostException;

class ChatClient{
    public static boolean checkArgs(String[] args){
        // Check args are usable or return false
        boolean success = false;
        try{

            if(args.length != 3)
                throw new IllegalArgumentException("You must provide 3 arguments");
            else if(args[0].length() >= 20)
                throw new IllegalArgumentException("The username value must be less than 20 characters.");
            else if(Integer.parseInt(args[2]) < 1025)
                throw new IllegalArgumentException("The provided port number must be greater than 1024.");
            else if(InetAddress.getByName(args[1]) == null) // Will throw if missing
                throw new IllegalArgumentException("The IP address provided must be valid");

            success = true;
        }catch(NumberFormatException e){
            System.out.println("The port number provided must be a port number.");
        }catch(UnknownHostException e){
            System.out.println("The IP address provided must be valid");
        }catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
        return success;
    }

    public static void printUsage(){
        System.out.println("java ChatClient <username> <multicast-ip> <port>");
        System.out.println("EXAMPLE: java ChatClient michaelwasher 239.0.202.1 40202");
        return;
    }

    // TODO: This shouldn't throw but clean up
    public static void main(String[] args) throws Exception
    {
        // Output usage and check input args
        if (!checkArgs(args)){
            printUsage();
            return;
        }
        // TODO: Check for -h / --help
        
        // Get IP address 
        InetAddress multicastAddress = InetAddress.getByName(args[1]);
        int portNum = Integer.parseInt(args[2]);
        String username = args[0];
        // TODO: Allow input of portNum... Throw if x < 1024
        // TODO: Allow users to provide a username within the chat system.
        // NOTE: Don't check if it's used.

        Client client = new Client(username, multicastAddress, portNum);
        client.start();
    }
}

// TODO: Add basic tests
