package astra.vm;

import java.util.HashMap;
import java.util.Map;

public class RuntimeEnvironment {
    private final Map<String, Boolean> conditions = new HashMap<>();

    public void setCondition(String name, boolean value) {
        conditions.put(name, value);
    }

    public boolean getCondition(String name) {
        if (!conditions.containsKey(name)) {
            System.out.println("RUNTIME WARNING: condition '" + name
                + "' was not set — defaulting to false");
            return false;
        }
        return conditions.get(name);
    }
}