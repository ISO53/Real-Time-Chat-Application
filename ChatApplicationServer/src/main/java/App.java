import java.io.*;
import java.util.Scanner;
import java.util.logging.Logger;

public class App {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    public static final int EXIT = 0;
    public static final int CARRY_ON = 1;

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int maxNumberOfConnections = 100;
        int port = 8080;

        writeAsciiArt();
        System.out.println("Welcome to the Chat Server.");
        System.out.println("Server address is -> " + serverAddress);
        System.out.println("Max Number of Connections is -> " + maxNumberOfConnections);
        System.out.println("Port Number is -> " + port);
        System.out.println("Would you like to change them? (y/n)");
        Scanner scanner = new Scanner(System.in);
        System.out.print("-> ");
        String changeChoice = scanner.nextLine();

        if (changeChoice.equals("y")) {
            System.out.println("Type the new server address.");
            System.out.print("-> ");
            serverAddress = scanner.nextLine();

            System.out.println("Type the max number of connections available. Must be positive integer.");
            System.out.print("-> ");
            maxNumberOfConnections = scanner.nextInt();

            System.out.println("Type the new server port. Must be positive and smaller than 65535.");
            System.out.print("-> ");
            port = scanner.nextInt();

            System.out.println("Changes are saved.");
        }

        System.out.println("To start to server type 'start'.");

        String isStart;
        do {
            System.out.print("-> ");
            isStart = scanner.nextLine();
        } while (!isStart.equals("start"));

        System.out.println("Server is starting...");
        Server server = Server.server;
        server.init(port, maxNumberOfConnections, serverAddress);
        server.start();
        System.out.println("Server is started!");

        int choice;
        int status;
        do {
            System.out.println("\nSelect a number from below.");
            System.out.println("0. Get Users");
            System.out.println("1. Get Chats");
            System.out.println("2. Shut Down The Server And Exit");
            System.out.print("-> ");
            choice = scanner.nextInt();
            status = choiceHandler(choice);
        } while (status != EXIT);

        System.out.println("Exiting from program, closing server...");
        server.stop();
        System.out.println("Server closed. Good bye.");
    }

    public static int choiceHandler(int choice) {

        switch (choice) {
            case 0 -> User.users.values().forEach(System.out::println);
            case 1 -> Chat.chats.forEach(System.out::println);
            case 2 -> {
                return EXIT;
            }
        }

        return CARRY_ON;
    }

    public static void writeAsciiArt() {
        String s = File.separator;
        String filePath = System.getProperty("user.dir") + String.format("%ssrc%smain%sresources%sascii_art.txt", s, s, s, s);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
