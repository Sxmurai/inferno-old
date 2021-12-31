package cope.inferno.util.internal;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {
    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
