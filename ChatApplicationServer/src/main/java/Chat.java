import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;

public class Chat {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    private final ArrayList<User> users;
    private final LinkedList<Message> messages;
    private final String chatName;
    private final int color;
    private final String id;

    public static final ArrayList<Chat> chats = new ArrayList<>();

    public Chat(String chatName) {
        this.users = new ArrayList<>();
        this.messages = new LinkedList<>();
        this.chatName = chatName;
        this.color = Utils.randomColor();
        this.id = Utils.uniqueId();
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Message> getMessagesAsArray() {
        return new ArrayList<>(messages);
    }

    @Override
    public String toString() {
        return String.format("{\"chatname\": \"%s\", \"color\": %d, \"id\": \"%s\", \"users\": %s}"
                , chatName
                , color
                , id
                , Utils.jsonify(users));
    }
}
