package exec.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private String ngram;
    private SuffixTree root;

    public OutputFragment(SuffixTree root,
                          String ngram,
                          Path tmpfile,
                          int appearances,
                          boolean redundants) {
        this.root = root;
        this.ngram = ngram;
        this.tmpfile = tmpfile;
        this.appearances = appearances;
        this.redundants = redundants;
    }

    public OutputFragment(SuffixTree root,
                          String ngram,
                          Path tmpfile) {
        this(root, ngram, tmpfile, 2, false);
    }

    public String call() {
        try (OutputStream output = Files.newOutputStream(tmpfile);
             PrintStream printStream = new PrintStream(output, true)) {
            root.accept(new OutputVisitor(ngram,
                                          appearances,
                                          redundants,
                                          printStream));

            return ngram;
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
