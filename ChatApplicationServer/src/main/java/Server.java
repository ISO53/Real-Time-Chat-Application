import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Server class to run using Singleton Pattern to prevent multiple Server objects being created.
 **/
public class Server implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    private Thread thread;

    private int port;
    private int maxNumberOfConnectionRequest;
    private InetAddress serverAddress;
    private boolean isRunning;
    private ServerSocket serverSocket;

    public static Server server = new Server();

    private Server() {
    }

    /**
     * Initializes the server with the specified port, maximum number of connection requests,
     * and server address.
     *
     * @param port                         the port number on which the server will listen for incoming connections
     * @param maxNumberOfConnectionRequest the maximum number of simultaneous connection requests the server can handle
     * @param serverAddress                the IP address or hostname of the server
     */
    public void init(int port, int maxNumberOfConnectionRequest, String serverAddress) {
        this.port = port;
        this.maxNumberOfConnectionRequest = maxNumberOfConnectionRequest;
        this.isRunning = false;
        this.thread = new Thread(this);

        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, e.toString());
            System.exit(1);
        }
    }

    @Override
    public void run() {

        try {
            this.serverSocket = new ServerSocket(this.port, this.maxNumberOfConnectionRequest, this.serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        LOGGER.log(Level.INFO, "Server started and listening on port " + port);

        while (isRunning) {
            try {
                SocketHandler socketHandler = new SocketHandler(serverSocket.accept());
                socketHandler.start();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Forced shut! " + e.getMessage());
            }
        }
    }

    public void start() {
        isRunning = true;
        this.thread.start();
    }

    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString());
        }
    }
}
