
package astra.toylang.ast;

public class UnaryExpressionNode extends ExpressionNode {

    public final String operator;
    public final ExpressionNode operand;

    public UnaryExpressionNode(
            String operator,
            ExpressionNode operand) {

        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public String print(String indent) {
        return indent + "Unary: " + operator + "\n"
             + operand.print(indent + "  ");
    }
}
