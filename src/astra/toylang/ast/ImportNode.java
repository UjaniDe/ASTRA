
package astra.toylang.ast;

public class ImportNode extends ToyNode {

    public final String name;

    public ImportNode(String name) {
        this.name = name;
    }

    @Override
    public String print(String indent) {
        return indent + "Import: " + name + "\n";
    }
}
