package task;

import java.io.PrintStream;
import java.util.concurrent.Callable;

import util.StreamStorageThreadFactory;
import index.SuffixTree;
import visitor.OutputVisitor;

public class OutputFragment implements Callable<String> {
    private int appearances;
    private boolean redundants;

    private String ngram;
    private SuffixTree root;

    public OutputFragment(SuffixTree root,
                          String ngram,
                          int appearances,
                          boolean redundants) {
        this.root = root;
        this.ngram = ngram;
        this.appearances = appearances;
        this.redundants = redundants;
    }

    public OutputFragment(SuffixTree root, String ngram) {
        this(root, ngram, 2, false);
    }

    public String call() {
        PrintStream printStream = StreamStorageThreadFactory
            .printStreamResource
            .get();

        root.accept(new OutputVisitor(ngram,
                                      appearances,
                                      redundants,
                                      printStream));

        return ngram;
    }
}
