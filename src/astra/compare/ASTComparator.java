
package astra.compare;

import astra.toylang.ast.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ASTComparator {

    private final List<StructuralChange> changes =
        new ArrayList<>();

    public List<StructuralChange> compare(
            ProgramNode before,
            ProgramNode after) {

        changes.clear();

        compareImports(before, after);
        compareFunctions(before, after);

        return new ArrayList<>(changes);
    }

    // Compare dependencies by name.

    private void compareImports(
            ProgramNode before,
            ProgramNode after) {

        Set<String> oldImports = new LinkedHashSet<>();
        Set<String> newImports = new LinkedHashSet<>();

        for (ImportNode node : before.imports) {
            oldImports.add(node.name);
        }

        for (ImportNode node : after.imports) {
            newImports.add(node.name);
        }

        for (String name : newImports) {
            if (!oldImports.contains(name)) {
                add(ChangeType.DEPENDENCY_ADDED,
                    null, null, name);
            }
        }

        for (String name : oldImports) {
            if (!newImports.contains(name)) {
                add(ChangeType.DEPENDENCY_REMOVED,
                    null, name, null);
            }
        }
    }

    // Match functions by their names.

    private void compareFunctions(
            ProgramNode before,
            ProgramNode after) {

        Map<String, FunctionNode> oldFunctions =
            indexFunctions(before);

        Map<String, FunctionNode> newFunctions =
            indexFunctions(after);

        for (String name : oldFunctions.keySet()) {
            if (!newFunctions.containsKey(name)) {
                add(ChangeType.FUNCTION_REMOVED,
                    name, name, null);
            }
        }

        for (String name : newFunctions.keySet()) {
            if (!oldFunctions.containsKey(name)) {
                add(ChangeType.FUNCTION_ADDED,
                    name, null, name);
            } else {
                FunctionNode oldFunction =
                    oldFunctions.get(name);

                FunctionNode newFunction =
                    newFunctions.get(name);

                if (!oldFunction.parameters.equals(
                        newFunction.parameters)) {

                    add(ChangeType.STATEMENT_CHANGED,
                        name,
                        "Parameters: " +
                            oldFunction.parameters,
                        "Parameters: " +
                            newFunction.parameters);
                }

                compareStatements(
                    name,
                    oldFunction.body,
                    newFunction.body
                );
            }
        }
    }

    private Map<String, FunctionNode> indexFunctions(
            ProgramNode program) {

        Map<String, FunctionNode> result =
            new LinkedHashMap<>();

        for (FunctionNode function : program.functions) {
            if (result.putIfAbsent(
                    function.name, function) != null) {

                throw new IllegalArgumentException(
                    "Duplicate function: " + function.name
                );
            }
        }

        return result;
    }

    // Compare corresponding statements.

    private void compareStatements(
            String functionName,
            List<StatementNode> oldStatements,
            List<StatementNode> newStatements) {

        int common = Math.min(
            oldStatements.size(),
            newStatements.size()
        );

        for (int i = 0; i < common; i++) {

            StatementNode oldStmt = oldStatements.get(i);
            StatementNode newStmt = newStatements.get(i);

            if (oldStmt instanceof IfNode oldIf
                    && newStmt instanceof IfNode newIf) {

                compareIf(functionName, oldIf, newIf);

            } else if (
                oldStmt instanceof CallStatementNode oldCall
                && newStmt instanceof CallStatementNode newCall
            ) {

                compareCalls(
                    functionName,
                    oldCall.call,
                    newCall.call
                );

            } else if (
                oldStmt instanceof AssignNode oldAssign
                && newStmt instanceof AssignNode newAssign
            ) {

                if (!oldAssign.name.equals(newAssign.name)
                    || !sameExpression(
                        oldAssign.value, newAssign.value)) {

                    add(ChangeType.STATEMENT_CHANGED,
                        functionName,
                        statementText(oldStmt),
                        statementText(newStmt));
                }

            } else if (
                oldStmt instanceof ReturnNode oldReturn
                && newStmt instanceof ReturnNode newReturn
            ) {

                if (!sameExpression(
                        oldReturn.value, newReturn.value)) {

                    add(ChangeType.STATEMENT_CHANGED,
                        functionName,
                        statementText(oldStmt),
                        statementText(newStmt));
                }

            } else if (!oldStmt.getClass().equals(
                    newStmt.getClass())) {

                add(ChangeType.STATEMENT_CHANGED,
                    functionName,
                    statementText(oldStmt),
                    statementText(newStmt));
            }
        }

        for (int i = common; i < oldStatements.size(); i++) {
            recordRemoved(functionName, oldStatements.get(i));
        }

        for (int i = common; i < newStatements.size(); i++) {
            recordAdded(functionName, newStatements.get(i));
        }
    }

    private void compareIf(
            String functionName,
            IfNode oldIf,
            IfNode newIf) {

        if (!sameExpression(
                oldIf.condition, newIf.condition)) {

            add(ChangeType.CONDITION_CHANGED,
                functionName,
                expressionText(oldIf.condition),
                expressionText(newIf.condition));
        }

        compareStatements(
            functionName,
            oldIf.thenBranch,
            newIf.thenBranch
        );

        compareStatements(
            functionName,
            oldIf.elseBranch == null
                ? List.of() : oldIf.elseBranch,
            newIf.elseBranch == null
                ? List.of() : newIf.elseBranch
        );
    }

    private void compareCalls(
            String functionName,
            CallExpressionNode oldCall,
            CallExpressionNode newCall) {

        if (!sameExpression(
                oldCall.callee, newCall.callee)) {

            add(ChangeType.CALL_REMOVED,
                functionName,
                expressionText(oldCall),
                null);

            add(ChangeType.CALL_ADDED,
                functionName,
                null,
                expressionText(newCall));

            return;
        }

        if (!sameArguments(
                oldCall.arguments, newCall.arguments)) {

            add(ChangeType.ARGUMENTS_CHANGED,
                functionName,
                expressionText(oldCall),
                expressionText(newCall));
        }
    }

    private boolean sameArguments(
            List<ExpressionNode> oldArgs,
            List<ExpressionNode> newArgs) {

        if (oldArgs.size() != newArgs.size()) {
            return false;
        }

        for (int i = 0; i < oldArgs.size(); i++) {
            if (!sameExpression(
                    oldArgs.get(i), newArgs.get(i))) {
                return false;
            }
        }

        return true;
    }

    private void recordAdded(
            String functionName,
            StatementNode stmt) {

        if (stmt instanceof CallStatementNode call) {
            add(ChangeType.CALL_ADDED,
                functionName,
                null,
                expressionText(call.call));
        } else {
            add(ChangeType.STATEMENT_CHANGED,
                functionName,
                null,
                statementText(stmt));
        }
    }

    private void recordRemoved(
            String functionName,
            StatementNode stmt) {

        if (stmt instanceof CallStatementNode call) {
            add(ChangeType.CALL_REMOVED,
                functionName,
                expressionText(call.call),
                null);
        } else {
            add(ChangeType.STATEMENT_CHANGED,
                functionName,
                statementText(stmt),
                null);
        }
    }

    // Canonical expression representation.
    // Parentheses make expression structure explicit.

    private String expressionText(ExpressionNode expr) {

        if (expr == null) {
            return "<none>";
        }

        if (expr instanceof IdentifierNode id) {
            return id.name;
        }

        if (expr instanceof LiteralNode literal) {
            if (literal.value instanceof String str) {
                return "\"" + str + "\"";
            }
            return String.valueOf(literal.value);
        }

        if (expr instanceof MemberAccessNode member) {
            return expressionText(member.object)
                + "." + member.member;
        }

        if (expr instanceof UnaryExpressionNode unary) {
            return unary.operator
                + "(" + expressionText(unary.operand) + ")";
        }

        if (expr instanceof BinaryExpressionNode binary) {
            return "(" + expressionText(binary.left)
                + " " + binary.operator + " "
                + expressionText(binary.right) + ")";
        }

        if (expr instanceof CallExpressionNode call) {
            List<String> args = new ArrayList<>();

            for (ExpressionNode arg : call.arguments) {
                args.add(expressionText(arg));
            }

            return expressionText(call.callee)
                + "(" + String.join(", ", args) + ")";
        }

        throw new IllegalArgumentException(
            "Unknown expression type: " +
                expr.getClass().getName()
        );
    }

    private boolean sameExpression(
            ExpressionNode first,
            ExpressionNode second) {

        if (first == null || second == null) {
            return first == second;
        }

        return Objects.equals(
            expressionText(first),
            expressionText(second)
        );
    }

    private String statementText(StatementNode stmt) {
        return stmt.print("").trim();
    }

    private void add(
            ChangeType type,
            String functionName,
            String before,
            String after) {

        changes.add(new StructuralChange(
            type, functionName, before, after
        ));
    }
}
