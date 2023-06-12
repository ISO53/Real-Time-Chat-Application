import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class User {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    private final String username;
    private final String password;
    private final int imageHash;
    private boolean isLoggedIn;
    private Socket socket;

    public static final Map<String, User> users = new HashMap<>();

    public User(String username, String password, Socket socket) {
        this.isLoggedIn = false;
        this.password = password;
        this.username = username;
        this.socket = socket;
        this.imageHash = Utils.randomNumber();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public String toString() {
        return String.format("{\"username\": \"%s\", \"imageHash\": %d}", this.username, this.imageHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return user.username.equals(this.username) && user.password.equals(this.password);
    }
}
