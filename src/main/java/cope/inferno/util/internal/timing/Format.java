package cope.inferno.util.internal.timing;

import java.util.function.Function;

public enum Format {
    TICKS((in) -> in * 50L),
    MS(null),
    SECONDS((in) -> in * 1000L);

    private final Function<Long, Long> conversion;

    Format(Function<Long, Long> in) {
        conversion = in;
    }

    public long convert(long in) {
        return conversion == null ? in : conversion.apply(in);
    }
}
