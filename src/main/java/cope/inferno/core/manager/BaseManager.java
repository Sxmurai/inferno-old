package cope.inferno.core.manager;

import cope.inferno.util.internal.Wrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseManager implements Wrapper {
    protected final Logger LOGGER = LogManager.getLogger(getClass().getSimpleName());

    public abstract void init();
}
