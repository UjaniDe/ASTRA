
package astra.toylang.ast;

public class CallStatementNode extends StatementNode {

    public final CallExpressionNode call;

    public CallStatementNode(CallExpressionNode call) {
        this.call = call;
    }

    @Override
    public String print(String indent) {
        return indent + "CallStatement\n"
             + call.print(indent + "  ");
    }
}
