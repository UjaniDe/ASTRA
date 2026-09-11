package astra.ast;

public class RunTestsNode extends Node {
    @Override
    public String print(String indent) {
        return indent + "RunTests\n";
    }
}