
package astra.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitSourceLoader implements AutoCloseable {

    private final Repository repository;

    public GitSourceLoader(String repositoryPath)
            throws IOException {

        repository = new FileRepositoryBuilder()
            .findGitDir(Path.of(repositoryPath).toFile())
            .readEnvironment()
            .build();

        if (repository.getObjectDatabase() == null
                || !repository.getObjectDatabase().exists()) {
            repository.close();
            throw new IOException(
                "Git repository not found: " + repositoryPath
            );
        }
    }

    public String loadFile(
            String revision,
            String filePath) throws IOException {

        ObjectId commitId =
            repository.resolve(revision + "^{commit}");

        if (commitId == null) {
            throw new IOException(
                "Unknown Git revision: " + revision
            );
        }

        try (RevWalk walk = new RevWalk(repository)) {

            RevCommit commit = walk.parseCommit(commitId);

            try (TreeWalk treeWalk = TreeWalk.forPath(
                    repository,
                    filePath,
                    commit.getTree())) {

                if (treeWalk == null) {
                    throw new IOException(
                        "File '" + filePath
                        + "' not found at revision '"
                        + revision + "'"
                    );
                }

                ObjectId blobId = treeWalk.getObjectId(0);
                ObjectLoader loader =
                    repository.open(blobId, Constants.OBJ_BLOB);

                return new String(
                    loader.getBytes(),
                    StandardCharsets.UTF_8
                );
            }
        }
    }

    @Override
    public void close() {
        repository.close();
    }
}
