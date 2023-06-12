import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public abstract class Utils {

    /**
     * Converts an {@code ArrayList} of objects into a JSON-like string representation.
     * If the input list is empty, the method returns "[]".
     *
     * @param list the ArrayList of objects to be converted
     * @return the JSON-like string representation of the input list
     */
    public static <T> String jsonify(ArrayList<T> list) {
        if (list.isEmpty()) {
            return "[]";
        }

        StringBuilder asString = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            asString.append(list.get(i).toString());
            if (i < list.size() - 1) {
                asString.append(", ");
            }
        }
        asString.append("]");

        return asString.toString();
    }

    public static int randomNumber() {
        Random random = new Random();
        int min = (int) Math.pow(10, 5 - 1);
        int max = (int) Math.pow(10, 5) - 1;
        return random.nextInt(max - min + 1) + min;
    }

    public static int randomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return (red << 16) | (green << 8) | blue;
    }

    public static String uniqueId() {
        return UUID.randomUUID().toString();
    }

}
