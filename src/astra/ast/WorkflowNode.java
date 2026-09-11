package astra.ast;

import java.util.List;

public class WorkflowNode extends Node {
    public final String name;
    public final List<Node> statements;

    public WorkflowNode(String name, List<Node> statements) {
        this.name = name;
        this.statements = statements;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Workflow: ").append(name).append("\n");
        for (Node stmt : statements) {
            sb.append(stmt.print(indent + "  "));
        }
        return sb.toString();
    }
}