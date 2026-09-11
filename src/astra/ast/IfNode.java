package astra.ast;

import java.util.List;

public class IfNode extends Node {
    public final String condition;
    public final List<Node> thenBranch;
    public final List<Node> elseBranch; // can be null

    public IfNode(String condition, List<Node> thenBranch, List<Node> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("If(").append(condition).append(")\n");
        for (Node stmt : thenBranch) sb.append(stmt.print(indent + "  "));
        if (elseBranch != null) {
            sb.append(indent).append("Else\n");
            for (Node stmt : elseBranch) sb.append(stmt.print(indent + "  "));
        }
        return sb.toString();
    }
}