package astra.git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class GitChangedFileDetector {

    public List<String> findChangedToyFiles(
            String repositoryPath,
            String oldRevision,
            String newRevision) throws IOException {

        try (Repository repository = new FileRepositoryBuilder()
                .findGitDir(Path.of(repositoryPath).toFile())
                .build()) {

            ObjectId oldId = repository.resolve(oldRevision + "^{commit}");
            ObjectId newId = repository.resolve(newRevision + "^{commit}");

            if (oldId == null || newId == null) {
                throw new IOException(
                    "Unable to resolve one or both Git revisions"
                );
            }

            try (RevWalk walk = new RevWalk(repository);
                 DiffFormatter formatter =
                     new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                RevCommit oldCommit = walk.parseCommit(oldId);
                RevCommit newCommit = walk.parseCommit(newId);

                formatter.setRepository(repository);
                formatter.setDetectRenames(true);

                List<String> changedFiles = new ArrayList<>();

                for (DiffEntry entry : formatter.scan(
                        oldCommit.getTree(),
                        newCommit.getTree())) {

                    if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY
                            && entry.getNewPath().endsWith(".toy")) {

                        changedFiles.add(entry.getNewPath());
                    }
                }

                return changedFiles;
            }
        }
    }
}
