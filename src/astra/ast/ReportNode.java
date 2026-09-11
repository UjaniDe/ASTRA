package astra.ast;

public class ReportNode extends Node {
    @Override
    public String print(String indent) {
        return indent + "Report\n";
    }
}