
package astra.toylang.ast;

import java.util.List;

public class IfNode extends StatementNode {

    public final ExpressionNode condition;
    public final List<StatementNode> thenBranch;
    public final List<StatementNode> elseBranch;

    public IfNode(
            ExpressionNode condition,
            List<StatementNode> thenBranch,
            List<StatementNode> elseBranch) {

        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("If\n");
        sb.append(indent).append("  Condition\n");
        sb.append(condition.print(indent + "    "));

        sb.append(indent).append("  Then\n");
        for (StatementNode stmt : thenBranch) {
            sb.append(stmt.print(indent + "    "));
        }

        if (elseBranch != null) {
            sb.append(indent).append("  Else\n");
            for (StatementNode stmt : elseBranch) {
                sb.append(stmt.print(indent + "    "));
            }
        }

        return sb.toString();
    }
}
