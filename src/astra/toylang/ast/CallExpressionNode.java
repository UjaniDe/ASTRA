
package astra.toylang.ast;

import java.util.List;

public class CallExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final List<ExpressionNode> arguments;

    public CallExpressionNode(
            ExpressionNode callee,
            List<ExpressionNode> arguments) {

        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("Call\n");
        sb.append(indent).append("  Callee\n");
        sb.append(callee.print(indent + "    "));

        for (ExpressionNode arg : arguments) {
            sb.append(indent).append("  Argument\n");
            sb.append(arg.print(indent + "    "));
        }

        return sb.toString();
    }
}
