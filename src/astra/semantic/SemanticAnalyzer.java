package astra.semantic;

import astra.ast.*;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable = new SymbolTable();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public SymbolTable getSymbolTable() { return symbolTable; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }

    public void analyze(WorkflowNode workflow) {
        if (workflow.statements.isEmpty()) {
            errors.add("Workflow '" + workflow.name + "' has an empty body — nothing to execute.");
        }
        for (Node stmt : workflow.statements) {
            visit(stmt);
        }
    }

    private void visit(Node node) {
        if (node instanceof CompareNode compare) {
            symbolTable.declareOrTouch(compare.branchA, SymbolTable.Kind.BRANCH);
            symbolTable.declareOrTouch(compare.branchB, SymbolTable.Kind.BRANCH);
            if (compare.branchA.equals(compare.branchB)) {
                errors.add("Compare statement compares branch '" + compare.branchA
                    + "' with itself — likely a mistake.");
            }
        } else if (node instanceof IfNode ifNode) {
            if (symbolTable.alreadyDeclared(ifNode.condition)) {
                warnings.add("Condition '" + ifNode.condition
                    + "' is checked more than once in this workflow.");
            }
            symbolTable.declareOrTouch(ifNode.condition, SymbolTable.Kind.CONDITION);
            for (Node s : ifNode.thenBranch) visit(s);
            if (ifNode.elseBranch != null) {
                for (Node s : ifNode.elseBranch) visit(s);
            }
        }
        // AnalyzeNode, RunTestsNode, ReportNode carry no identifiers — nothing to register
    }
}