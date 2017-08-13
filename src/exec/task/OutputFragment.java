package exec.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import exec.Simulator;
import util.LogAgent;
import util.SuffixTree;
import visitor.OutputVisitor;

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

        try (PrintStream printStream =
             new PrintStream(Files.newOutputStream(tmpfile), true)) {
            int i = 0; // just for accounting!
            for (String ngram : ngrams) {
                LogAgent
                    .LOGGER
                    .info(String.format("%s %d/%d",
                                        tmpfile.getFileName().toString(),
                                        ++i,
                                        ngrams.size()));

                root
                    .getChildren()
                    .get(ngram)
                    .accept(new OutputVisitor(ngram,
                                              appearances,
                                              redundants,
                                              printStream));

                results.add(ngram);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return results.toString();
    }
}
