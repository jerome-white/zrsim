package util;

import java.util.concurrent.Callable;

import tree.SuffixTree;
import visitor.MarkRedundantVisitor;

public class TermSelector implements Callable<String> {
    private SuffixTree root;
    private String ngram;

    public TermSelector(SuffixTree root, String ngram) {
	this.root = root;
	this.ngram = ngram;
    }

    public String call() {
	SuffixTree child = root.getChildren().get(ngram);
	child.accept(new MarkRedundantVisitor(ngram, root));

	return ngram;
    }
}
