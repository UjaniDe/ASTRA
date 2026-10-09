
package astra.compare;

public class StructuralChange {

    public final ChangeType type;
    public final String functionName;
    public final String before;
    public final String after;

    public StructuralChange(
            ChangeType type,
            String functionName,
            String before,
            String after) {

        this.type = type;
        this.functionName = functionName;
        this.before = before;
        this.after = after;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("[")
          .append(type.toString().replace('_', ' '))
          .append("]\n");

        if (functionName != null) {
            sb.append("Function: ")
              .append(functionName)
              .append("\n");
        }

        if (before != null) {
            sb.append("Before: ")
              .append(before)
              .append("\n");
        }

        if (after != null) {
            sb.append("After:  ")
              .append(after)
              .append("\n");
        }

        return sb.toString();
    }
}
