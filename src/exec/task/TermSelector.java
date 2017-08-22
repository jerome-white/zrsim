package exec.task;

import java.util.concurrent.Callable;

import index.SuffixTree;
import visitor.MarkRedundantVisitor;

public class TermSelector implements Callable<String> {
    private String ngram;
    private SuffixTree root;
    private SuffixTree child;

    public TermSelector(SuffixTree root, SuffixTree child, String ngram) {
        this.root = root;
        this.child = child;
        this.ngram = ngram;
    }

    public String call() {
        child.accept(new MarkRedundantVisitor(ngram, root));

        return ngram;
    }
}
