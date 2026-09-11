package astra.ast;

public class CompareNode extends Node {
    public final String branchA, branchB;

    public CompareNode(String branchA, String branchB) {
        this.branchA = branchA;
        this.branchB = branchB;
    }

    @Override
    public String print(String indent) {
        return indent + "Compare(\"" + branchA + "\" with \"" + branchB + "\")\n";
    }
}