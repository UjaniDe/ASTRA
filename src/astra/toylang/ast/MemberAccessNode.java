
package astra.toylang.ast;

public class MemberAccessNode extends ExpressionNode {

    public final ExpressionNode object;
    public final String member;

    public MemberAccessNode(ExpressionNode object, String member) {
        this.object = object;
        this.member = member;
    }

    @Override
    public String print(String indent) {
        return indent + "MemberAccess: " + member + "\n"
             + object.print(indent + "  ");
    }
}
