
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

    // ==========================================
    // DEPENDENCY COMPARISON
    // ==========================================

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

    // ==========================================
    // FUNCTION COMPARISON
    // ==========================================

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

    // ==========================================
    // LCS-BASED STATEMENT COMPARISON
    // ==========================================

    private void compareStatements(
            String functionName,
            List<StatementNode> oldStatements,
            List<StatementNode> newStatements) {

        int m = oldStatements.size();
        int n = newStatements.size();

        int[][] dp = new int[m + 1][n + 1];

        // Build the Longest Common Subsequence table.

        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {

                if (sameStatement(
                        oldStatements.get(i),
                        newStatements.get(j))) {

                    dp[i][j] =
                        1 + dp[i + 1][j + 1];

                } else {

                    dp[i][j] = Math.max(
                        dp[i + 1][j],
                        dp[i][j + 1]
                    );
                }
            }
        }

        // Walk aligned statements and unmatched regions.

        int i = 0;
        int j = 0;

        while (i < m || j < n) {

            if (i < m && j < n
                    && sameStatement(
                        oldStatements.get(i),
                        newStatements.get(j))) {

                compareMatchedStatements(
                    functionName,
                    oldStatements.get(i),
                    newStatements.get(j)
                );

                i++;
                j++;

            } else if (
                i < m && j < n
                && canMatchModification(
                    oldStatements.get(i),
                    newStatements.get(j))
                && dp[i + 1][j + 1] >= dp[i + 1][j]
                && dp[i + 1][j + 1] >= dp[i][j + 1]
            ) {

                compareMatchedStatements(
                    functionName,
                    oldStatements.get(i),
                    newStatements.get(j)
                );

                i++;
                j++;

            } else if (
                j < n
                && (i == m
                    || dp[i][j + 1] >= dp[i + 1][j])
            ) {

                recordAdded(
                    functionName,
                    newStatements.get(j)
                );

                j++;

            } else {

                recordRemoved(
                    functionName,
                    oldStatements.get(i)
                );

                i++;
            }
        }
    }

    // ==========================================
    // STATEMENT MATCHING
    // ==========================================

    private boolean sameStatement(
            StatementNode first,
            StatementNode second) {

        if (!first.getClass().equals(
                second.getClass())) {
            return false;
        }

        if (first instanceof IfNode a
                && second instanceof IfNode b) {

            return true;
        }

        if (first instanceof CallStatementNode a
                && second instanceof CallStatementNode b) {

            return sameExpression(
                a.call,
                b.call
            );
        }

        if (first instanceof AssignNode a
                && second instanceof AssignNode b) {

            return a.name.equals(b.name)
                && sameExpression(
                    a.value,
                    b.value
                );
        }

        if (first instanceof ReturnNode a
                && second instanceof ReturnNode b) {

            return sameExpression(
                a.value,
                b.value
            );
        }

        return statementText(first).equals(
            statementText(second)
        );
    }

    private boolean canMatchModification(
            StatementNode first,
            StatementNode second) {

        if (first instanceof IfNode
                && second instanceof IfNode) {
            return true;
        }

        if (first instanceof AssignNode a
                && second instanceof AssignNode b) {

            return a.name.equals(b.name);
        }

        if (first instanceof CallStatementNode a
                && second instanceof CallStatementNode b) {

            return sameExpression(
                a.call.callee,
                b.call.callee
            );
        }

        if (first instanceof ReturnNode
                && second instanceof ReturnNode) {
            return true;
        }

        return false;
    }

    // ==========================================
    // MATCHED STATEMENT ANALYSIS
    // ==========================================

    private void compareMatchedStatements(
            String functionName,
            StatementNode oldStmt,
            StatementNode newStmt) {

        if (oldStmt instanceof IfNode oldIf
                && newStmt instanceof IfNode newIf) {

            compareIf(
                functionName,
                oldIf,
                newIf
            );

        } else if (
            oldStmt instanceof CallStatementNode oldCall
            && newStmt instanceof CallStatementNode newCall
        ) {

            compareCalls(
                functionName,
                oldCall.call,
                newCall.call
            );

        } else if (!sameStatement(oldStmt, newStmt)) {

            add(ChangeType.STATEMENT_CHANGED,
                functionName,
                statementText(oldStmt),
                statementText(newStmt));
        }
    }

    // ==========================================
    // CONDITIONAL COMPARISON
    // ==========================================

    private void compareIf(
            String functionName,
            IfNode oldIf,
            IfNode newIf) {

        if (!sameExpression(
                oldIf.condition,
                newIf.condition)) {

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
                ? List.of()
                : oldIf.elseBranch,
            newIf.elseBranch == null
                ? List.of()
                : newIf.elseBranch
        );
    }

    // ==========================================
    // FUNCTION CALL COMPARISON
    // ==========================================

    private void compareCalls(
            String functionName,
            CallExpressionNode oldCall,
            CallExpressionNode newCall) {

        if (!sameExpression(
                oldCall.callee,
                newCall.callee)) {

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
                oldCall.arguments,
                newCall.arguments)) {

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
                    oldArgs.get(i),
                    newArgs.get(i))) {

                return false;
            }
        }

        return true;
    }

    // ==========================================
    // ADDED AND REMOVED STATEMENTS
    // ==========================================

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

    // ==========================================
    // EXPRESSION REPRESENTATION
    // ==========================================

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
                + "("
                + expressionText(unary.operand)
                + ")";
        }

        if (expr instanceof BinaryExpressionNode binary) {

            return "("
                + expressionText(binary.left)
                + " "
                + binary.operator
                + " "
                + expressionText(binary.right)
                + ")";
        }

        if (expr instanceof CallExpressionNode call) {

            List<String> args = new ArrayList<>();

            for (ExpressionNode arg : call.arguments) {
                args.add(expressionText(arg));
            }

            return expressionText(call.callee)
                + "("
                + String.join(", ", args)
                + ")";
        }

        throw new IllegalArgumentException(
            "Unknown expression type: "
                + expr.getClass().getName()
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

    private String statementText(
            StatementNode stmt) {

        return stmt.print("").trim();
    }

    // ==========================================
    // CHANGE RECORDING
    // ==========================================

    private void add(
            ChangeType type,
            String functionName,
            String before,
            String after) {

        changes.add(
            new StructuralChange(
                type,
                functionName,
                before,
                after
            )
        );
    }
}
