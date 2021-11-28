package cope.inferno.impl.settings;

public class EnumConverter {
    private final Class<? extends Enum> clazz;

    public EnumConverter(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    public static int currentEnum(Enum clazz) {
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
            Enum e = ((Enum[]) clazz.getClass().getEnumConstants())[i];
            if (e.name().equalsIgnoreCase(clazz.name())) {
                return i;
            }
        }

        return -1;
    }

    public static Enum increaseEnum(Enum clazz) {
        int index = currentEnum(clazz);

        for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
            Enum e = ((Enum[]) clazz.getClass().getEnumConstants())[i];
            if (i == index + 1) {
                return e;
            }
        }

        return ((Enum[]) clazz.getClass().getEnumConstants())[0];
    }

    public static String getActualName(Enum clazz) {
        return getActualName(clazz.name());
    }

    public static String getActualName(String name) {
        return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1).toLowerCase().replaceAll("_", " ");
    }


    public Enum doBackward(String name) {
        try {
            return Enum.valueOf(clazz, name);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}