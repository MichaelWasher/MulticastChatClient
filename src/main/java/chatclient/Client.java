package chatclient;

/**
 *
 * @author  Michael Washer
 * @since   2014
 * 
*/
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.net.SocketException;
import java.util.logging.Logger;

class Client {

    private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());

    // Properties
    MulticastSocket ms;
    int portNum = 40202;
    InetAddress multicastAddress;
    String messagePrefix = "";
    String username = "";

    ReceiveMessageWorker receiveThread;

    String quittingKey = "/quit";

    public Client(String username, InetAddress multicastAddress, int portNum) throws IllegalArgumentException {
        // Set Up Client
        try {
            // Set Up Client
            this.multicastAddress = multicastAddress;
            this.portNum = portNum;
            this.ms = new MulticastSocket(portNum);
            this.ms.joinGroup(this.multicastAddress);
            this.username = username;
            // Set message prefix
            messagePrefix = String.format("%s(%s)\t", this.username, this.multicastAddress.getHostAddress());
        } catch (Exception e) {
            LOGGER.info("One of the arguments provided were unable to be processed.");
            LOGGER.info(e.getStackTrace().toString());
            throw new IllegalArgumentException("One of the arguments provided were unable to be processed.");
        }
    }

    // Used for mocking values for Unit Tests
    protected Client(String username, InetAddress multicastAddress, int portNum, Socket ms)
            throws IllegalArgumentException {
        // TODO init project with dep injections
    }

    private void displayOpeningMessage() {
        // Ouput message so User knows how to leave
        System.out.print("\033[H\033[2J"); // Clear the page
        System.out.flush();
        System.out.println(String.format("Welcome to ChatClient %s. Type /quit at any time to exit.", this.username));
        System.out.println("-------------------------------------------------------\n");
    }

    public void start() {
        try {
            this.displayOpeningMessage();

            // Start Looking for Messages to Receive
            this.open();

            // Main Chat Loop
            Scanner s = new Scanner(System.in);
            while (true) {
                String message = s.nextLine().toLowerCase();

                // Special Cases
                if (message.length() < 1)
                    continue;
                if (message.toLowerCase().equals(quittingKey))
                    break;

                // Clear typing line
                System.out.print("\033[1A"); // Move up
                System.out.print("\033[2K"); // Erase line content

                sendMessage(messagePrefix + message);
            }
            s.close();
        } catch (Exception e) {
            LOGGER.info(e.getStackTrace().toString());
            LOGGER.info("Closing now.");
        }finally{
            // Close all Open Streams and Desconstruct Method
            this.halt();
        }
    }

    public void open() {
        this.receiveThread = new ReceiveMessageWorker(ms);
        this.receiveThread.start();
    }

    public void halt() {
        try {
            this.receiveThread.halt();
            this.ms.close();
            this.receiveThread.join();
        } catch (Exception e) {
            LOGGER.info("Unable to close cleanly. Force closing...");
            LOGGER.info(e.getStackTrace().toString());
        }
    }

    public void sendMessage(String messageContents) throws IOException {
        // Create Packet
        DatagramPacket msgPacket = new DatagramPacket(messageContents.getBytes(), messageContents.length(),
                multicastAddress, portNum);
        // Send Packet
        ms.send(msgPacket);
    }

    protected class ReceiveMessageWorker extends Thread {
        protected boolean loop = true;
        protected MulticastSocket ms;
        protected DateTimeFormatter messageTimeFormatter;

        public ReceiveMessageWorker(MulticastSocket ms) {
            this.ms = ms;
        }

        private DatagramPacket receivePacket() throws IOException {
            byte[] buf = new byte[1000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            messageTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            ms.receive(recv);
            return recv;
        }

        private String buildMessageFromPacket(DatagramPacket packet) {
            // Create String
            LocalDateTime now = LocalDateTime.now();
            String outputString = String.format("%s : %s", messageTimeFormatter.format(now),
                    new String(packet.getData(), StandardCharsets.UTF_8));
            return outputString;
        }

        public void halt() {
            this.loop = false;
        }

        public void run() {
            try {
                while (loop) {
                    DatagramPacket dp = this.receivePacket();
                    String outputString = this.buildMessageFromPacket(dp);
                    System.out.println(outputString);
                }
            } catch (SocketException se) {
                // Closing the socket as expected
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
