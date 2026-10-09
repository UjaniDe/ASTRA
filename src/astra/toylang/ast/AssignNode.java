
package astra.toylang.ast;

public class AssignNode extends StatementNode {

    public final String name;
    public final ExpressionNode value;

    public AssignNode(String name, ExpressionNode value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String print(String indent) {
        return indent + "Assignment: " + name + "\n"
             + value.print(indent + "  ");
    }
}
