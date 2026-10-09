
package astra.toylang.ast;

public class BinaryExpressionNode extends ExpressionNode {

    public final ExpressionNode left;
    public final String operator;
    public final ExpressionNode right;

    public BinaryExpressionNode(
            ExpressionNode left,
            String operator,
            ExpressionNode right) {

        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String print(String indent) {
        return indent + "Binary: " + operator + "\n"
             + left.print(indent + "  ")
             + right.print(indent + "  ");
    }
}
