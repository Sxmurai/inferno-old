package cope.inferno.util.text;

public class FormatUtil {
    public static String formatName(String in) {
        return Character.toString(in.charAt(0)).toUpperCase() +
                in.substring(1).toLowerCase().replaceAll("_", " ");
    }
}
