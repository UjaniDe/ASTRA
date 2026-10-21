package astra.vm;

import java.util.HashMap;
import java.util.Map;

public class RuntimeEnvironment {
    private final Map<String, Boolean> conditions = new HashMap<>();

    private String repositoryPath;
    private String sourceFilePath;

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

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }
}
