package simulate.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import tree.SuffixTree;
import visitor.OutputVisitor;
import simulate.Manager;

public class OutputFragment implements Callable<String> {
    private int appearances;
    private boolean redundants;

    private Path path;
    private Path tmpfile;
    private SuffixTree root;
    private Collection<String> ngrams;

    public OutputFragment(SuffixTree root,
                          Collection<String> ngrams,
                          Path tmpfile,
                          int appearances,
                          boolean redundants) {
        this.root = root;
        this.ngrams = ngrams;
        this.tmpfile = tmpfile;
        this.appearances = appearances;
        this.redundants = redundants;
    }

    public OutputFragment(SuffixTree root,
                          Collection<String> ngrams,
                          Path tmpfile) {
        this(root, ngrams, tmpfile, 2, false);
    }

    public String call() {
        StringJoiner results = new StringJoiner(",");

        Manager.LOGGER.info(tmpfile.getFileName().toString());

        try (PrintStream printStream =
             new PrintStream(Files.newOutputStream(tmpfile), true)) {
            int i = 0; // just for accounting!
            for (String n : ngrams) {

                Manager.LOGGER.info(tmpfile.getFileName().toString() + " " +
                                    ++i + "/" + ngrams.size());

                OutputVisitor visitor =
                    new OutputVisitor(n, appearances, redundants, printStream);
                root.getChildren().get(n).accept(visitor);

                results.add(n);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return results.toString();
    }
}
