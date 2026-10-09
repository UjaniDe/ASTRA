
package astra.git;

public class GitSourceLoaderDemo {

    public static void main(String[] args)
            throws Exception {

        if (args.length != 3) {
            System.out.println(
                "Usage: GitSourceLoaderDemo "
                + "<repository> <revision> <file>"
            );
            return;
        }

        try (GitSourceLoader loader =
                new GitSourceLoader(args[0])) {

            String source = loader.loadFile(
                args[1],
                args[2]
            );

            System.out.println(
                "=== GIT SOURCE RETRIEVAL ==="
            );

            System.out.println("Revision: " + args[1]);
            System.out.println("File: " + args[2]);
            System.out.println();
            System.out.print(source);
        }
    }
}
