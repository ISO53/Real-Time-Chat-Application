import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    private static final String MESSAGE_END = "######";
    private static final char REGISTER = '0';
    private static final char ALREADY_EXIST = '1';
    private static final char LOGIN = '2';
    private static final char DOESNT_EXIST = '3';
    private static final char WRONG_PASSWORD = '4';
    private static final char GET_USERS = '5';
    private static final char PERMISSION_DENIED = '6';
    private static final char SUCCESSFUL_REGISTER = '7';
    private static final char SUCCESSFUL_LOGIN = '8';
    private static final char GET_CHATS = '9';
    private static final char GET_CHAT = 'a';
    private static final char IS_THERE_A_CHAT = 'b';
    private static final char NO_CHAT = 'c';
    private static final char CREATE_CHAT = 'd';
    private static final char GET_MESSAGES = 'e';
    private static final char ALREADY_LOGGED_IN = 'f';
    private static final char SEND_MESSAGE = 'g';
    private static final char ADD_USER_TO_CHAT = 'h';

    private final Socket socket;
    private final Thread thread;
    private boolean isRunning;
    private User user;

    public SocketHandler(Socket socket) {
        this.socket = socket;
        this.isRunning = false;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        flushOutput();
        LOGGER.log(Level.INFO, "Connection Established: " + this.socket.getRemoteSocketAddress());

        while (isRunning) {

            String message = read();

            if (message == null || message.equals("")) {
                // Connection closing

                break;
            }

            System.out.println("MESSAGE -> " + message);
            messageHandler(message);
        }

        // User disconnected
        user.setLoggedIn(false);

        LOGGER.log(Level.INFO, user.getUsername() + "'s connection closed: " + this.socket.getRemoteSocketAddress());
    }

    /**
     * Handles the incoming message received by the server.
     * The method processes different types of messages based on their identifiers:
     *
     * @param message the incoming message received by the server
     */
    private void messageHandler(String message) {
        String[] messages = message.split(MESSAGE_END);

        for (String msg : messages) {
            if (msg.length() == 0) {
                return;
            }

            char identifier = msg.charAt(0);
            String clippedMessage = msg.substring(1);

            switch (identifier) {
                case REGISTER -> {
                    String[] auth = clippedMessage.split(",");
                    if (auth.length < 2) {
                        return;
                    }

                    String username = auth[0];
                    String password = auth[1];

                    if (User.users.containsKey(username)) {
                        send(socket, ALREADY_EXIST, "");
                        return;
                    }

                    user = new User(username, password, socket);
                    User.users.put(user.getUsername(), user);

                    LOGGER.log(Level.INFO, String.format("User [%s] successfully registered.", user.getUsername()));
                    send(socket, SUCCESSFUL_REGISTER, "");
                }
                case LOGIN -> {
                    String[] auth = clippedMessage.split(",");
                    if (auth.length < 2) {
                        return;
                    }

                    String username = auth[0];
                    String password = auth[1];

                    user = User.users.get(username);

                    if (user == null) {
                        send(socket, DOESNT_EXIST, "");
                        return;
                    }

                    if (user.isLoggedIn()) {
                        send(socket, ALREADY_LOGGED_IN, "");
                        return;
                    }

                    User tempUser = User.users.get(username);

                    if (!tempUser.getPassword().equals(password)) {
                        send(socket, WRONG_PASSWORD, "");
                        return;
                    }

                    // Login successful
                    tempUser.setLoggedIn(true);
                    user = tempUser;
                    user.setSocket(this.socket);

                    LOGGER.log(Level.INFO, String.format("User [%s] successfully logged in.", user.getUsername()));
                    send(socket, SUCCESSFUL_LOGIN, user.getUsername());

                    // Send every client the complete users array because someone is connected
                    broadcast(GET_USERS, Utils.jsonify(getOnlineUsers()));
                }
                case GET_USERS -> {
                    if (user == null) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    if (!user.isLoggedIn()) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    send(user.getSocket(), GET_USERS, Utils.jsonify(getOnlineUsers()));
                }
                case GET_CHATS -> {
                    if (user == null) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    if (!user.isLoggedIn()) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    send(user.getSocket(), GET_CHATS, Utils.jsonify(getUsersChats(user)));
                }
                case IS_THERE_A_CHAT -> {
                    if (user == null) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    if (!user.isLoggedIn()) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    User tempUser = User.users.get(clippedMessage);

                    if (tempUser == null) {
                        send(user.getSocket(), DOESNT_EXIST, "");
                        return;
                    }

                    Chat tempChat = Chat.chats
                            .stream()
                            .filter(c -> c.getUsers().size() == 2 && c.getUsers().contains(tempUser) && c.getUsers().contains(user))
                            .findFirst()
                            .orElse(null);

                    if (tempChat == null) {
                        // There is no chat between these users.
                        send(user.getSocket(), NO_CHAT, tempUser.getUsername());
                    } else {
                        send(user.getSocket(), GET_CHAT, tempChat.toString());
                    }
                }
                case CREATE_CHAT -> {
                    if (user == null) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    if (!user.isLoggedIn()) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    String[] infos = clippedMessage.split(",");
                    String chatName = infos[0];
                    String userName = infos[1];

                    User otherUser = User.users.get(userName);

                    Chat chat = new Chat(chatName);
                    chat.addUser(otherUser);
                    chat.addUser(user);

                    Chat.chats.add(chat);

                    send(user.getSocket(), GET_CHATS, Utils.jsonify(getUsersChats(user)));
                    send(otherUser.getSocket(), GET_CHATS, Utils.jsonify(getUsersChats(otherUser)));

                    send(user.getSocket(), GET_CHAT, chat.toString());
                    send(otherUser.getSocket(), GET_CHAT, chat.toString());
                }
                case GET_MESSAGES -> {
                    if (user == null) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    if (!user.isLoggedIn()) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    Chat chat = Chat.chats
                            .stream()
                            .filter(c -> c.getId().equals(clippedMessage))
                            .findFirst()
                            .orElse(null);

                    if (chat == null) {
                        return;
                    }

                    send(user.getSocket(), GET_MESSAGES, chat.getId() + "," + Utils.jsonify(chat.getMessagesAsArray()));
                }
                case SEND_MESSAGE -> {
                    if (user == null) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    if (!user.isLoggedIn()) {
                        send(socket, PERMISSION_DENIED, "");
                        return;
                    }

                    String[] infos = clippedMessage.split(",", 5);
                    String chatId = infos[0];
                    int fileType = Integer.parseInt(infos[1]);

                    Message messageObject;

                    if (fileType == Message.TYPE_FILE) {
                        int fileFormat = Integer.parseInt(infos[2]);
                        String fileName = infos[3];

                        String byteArrayAsString = infos[4];
                        byte[] byteArray = byteArrayAsString.getBytes(StandardCharsets.UTF_8);

                        byte[] data = new byte[byteArray.length];
                        System.arraycopy(byteArray, 0, data, 0, byteArray.length);
                        byte[] encodedData = Base64.getEncoder().encode(data);

                        messageObject = new Message(user, Message.TYPE_FILE, new String(encodedData, StandardCharsets.UTF_8), fileFormat, fileName);
                    } else {
                        String comingMessage = infos[2];
                        messageObject = new Message(user, Message.TYPE_TEXT, comingMessage);
                    }

                    Chat chat = Chat.chats
                            .stream()
                            .filter(c -> c.getId().equals(chatId))
                            .findFirst()
                            .orElse(null);

                    System.out.println("chat found " + chat);

                    if (chat == null) {
                        return;
                    }

                    chat.addMessage(messageObject);

                    for (User chatUser : chat.getUsers()) {
                        send(chatUser.getSocket()
                                , GET_MESSAGES,
                                chat.getId() + "," + Utils.jsonify(chat.getMessagesAsArray()));
                    }
                }
                case ADD_USER_TO_CHAT -> {
                    String[] infos = clippedMessage.split(",");
                    String chatId = infos[0];
                    String userToAdd = infos[1];

                    Chat chatRoom = Chat.chats.stream()
                            .filter(chat -> chat.getId().equals(chatId))
                            .findFirst()
                            .orElse(null);

                    if (chatRoom == null) {
                        LOGGER.log(Level.WARNING, "Chat not found!");
                        return;
                    }

                    User foundUser = User.users.get(userToAdd);
                    if (foundUser == null) {
                        send(user.getSocket(), DOESNT_EXIST, "");
                        return;
                    }

                    chatRoom.addUser(foundUser);
                }
            }
        }
    }

    public void broadcast(char identifier, String message) {
        for (User user : getOnlineUsers()) {
            send(user.getSocket(), identifier, message);
        }
    }

    public ArrayList<User> getOnlineUsers() {
        return User.users.values()
                .stream()
                .filter(User::isLoggedIn)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public ArrayList<Chat> getUsersChats(User user) {
        return Chat.chats
                .stream()
                .filter(chat -> chat.getUsers().contains(user))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void send(Socket socket, char identifier, String message) {
        String text = String.format("%c%s%s", identifier, message, MESSAGE_END);
        try {
            socket.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
            //socket.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString());
        }
    }

    private String read() {
        try {
            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[2097152]; // 2mb because for files.
            int numBytesRead;
            numBytesRead = input.read(buffer);

            return new String(buffer, 0, numBytesRead, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString());
            e.printStackTrace();
            return null;
        }
    }

    public void start() {
        isRunning = true;
        this.thread.start();
    }

    public void stop() {
        isRunning = false;
    }

    public void kill() {
        try {
            this.socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString());
            //thread.stop();
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString());
            }
        }
    }

    private void flushOutput() {
        try {
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

}
