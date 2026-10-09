
package astra.git;

import astra.compare.StructuralChange;
import java.util.List;

public class GitComparisonDemo {

    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            System.out.println(
                "Usage: GitComparisonDemo "
                + "<repository> <oldRevision> "
                + "<newRevision> <filePath>"
            );
            return;
        }

        GitComparisonService service =
            new GitComparisonService(args[0]);

        List<StructuralChange> changes =
            service.compare(args[1], args[2], args[3]);

        System.out.println(
            "=== ASTRA GIT STRUCTURAL ANALYSIS ==="
        );

        System.out.println(
            "Comparing: " + args[1]
            + " -> " + args[2]
        );

        System.out.println("File: " + args[3]);

        if (changes.isEmpty()) {
            System.out.println(
                "No structural changes detected."
            );
            return;
        }

        for (StructuralChange change : changes) {
            System.out.println();
            System.out.print(change);
        }

        System.out.println(
            "\nTotal structural changes: "
            + changes.size()
        );
    }
}
