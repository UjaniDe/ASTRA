
package astra.toylang.ast;

public class ReturnNode extends StatementNode {

    public final ExpressionNode value;

    public ReturnNode(ExpressionNode value) {
        this.value = value;
    }

    @Override
    public String print(String indent) {
        if (value == null) {
            return indent + "Return\n";
        }

        return indent + "Return\n"
             + value.print(indent + "  ");
    }
}
