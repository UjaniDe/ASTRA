
package astra.toylang.ast;

public class LiteralNode extends ExpressionNode {

    public final Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    @Override
    public String print(String indent) {
        return indent + "Literal: " + value + "\n";
    }
}
