package astra.semantic;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    public enum Kind { CONDITION, BRANCH }

    public static class Entry {
        public final String name;
        public final Kind kind;
        public int usageCount = 1;

        public Entry(String name, Kind kind) {
            this.name = name;
            this.kind = kind;
        }
    }

    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public boolean alreadyDeclared(String name) {
        return entries.containsKey(name);
    }

    public void declareOrTouch(String name, Kind kind) {
        Entry existing = entries.get(name);
        if (existing != null) {
            existing.usageCount++;
        } else {
            entries.put(name, new Entry(name, kind));
        }
    }

    public Collection<Entry> allEntries() {
        return entries.values();
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-12s %s%n", "NAME", "KIND", "USES"));
        for (Entry e : entries.values()) {
            sb.append(String.format("%-20s %-12s %d%n", e.name, e.kind, e.usageCount));
        }
        return sb.toString();
    }
}