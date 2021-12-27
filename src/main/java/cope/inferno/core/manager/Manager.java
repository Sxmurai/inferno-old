package cope.inferno.core.manager;

import java.util.ArrayList;

public abstract class Manager<T> extends BaseManager {
    protected final ArrayList<T> modules = new ArrayList<>();

    public ArrayList<T> getModules() {
        return modules;
    }
}
