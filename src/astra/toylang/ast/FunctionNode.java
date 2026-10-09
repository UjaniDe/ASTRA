
package astra.toylang.ast;

import java.util.List;

public class FunctionNode extends ToyNode {

    public final String name;
    public final List<String> parameters;
    public final List<StatementNode> body;

    public FunctionNode(
            String name,
            List<String> parameters,
            List<StatementNode> body) {

        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public String print(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
          .append("Function: ")
          .append(name)
          .append("\n");

        for (String parameter : parameters) {
            sb.append(indent)
              .append("  Parameter: ")
              .append(parameter)
              .append("\n");
        }

        for (StatementNode statement : body) {
            sb.append(statement.print(indent + "  "));
        }

        return sb.toString();
    }
}
