import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.util.logging.Logger;

public class Message {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_FILE = 2;

    private final int PNG = 0;
    private final int JPG = 1;
    private final int JPEG = 2;
    private final int PDF = 3;
    private final int DOC = 4;
    private final int DOCX = 5;
    private final int TXT = 6;
    private final int XLSX = 7;

    private final User owner;
    private final Integer type;
    private final String message;
    private final Integer fileFormat;
    private final String fileName;

    public Message(User owner, Integer type, String message) {
        this.owner = owner;
        this.type = type;
        this.message = message;
        this.fileFormat = -1;
        this.fileName = null;
    }

    public Message(User owner, Integer type, String message, int fileFormat, String fileName) {
        this.owner = owner;
        this.type = type;
        this.message = message;
        this.fileFormat = fileFormat;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return String.format("{\"owner\": \"%s\", \"type\": %d, \"message\": \"%s\", \"file_format\": %d, \"file_name\": \"%s\"}"
                , owner.getUsername()
                , type
                , message
                , fileFormat
                , fileName
        );
    }
}
