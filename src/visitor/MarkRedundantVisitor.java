package visitor;

import java.util.NoSuchElementException;

import tree.SuffixTree;
import util.StringWindow;
import util.OuterStringWindow;

public class MarkRedundantVisitor implements SuffixTreeVisitor {
    private String ngram;
    private SuffixTree root;
    private StringWindow stringWindow;

    public MarkRedundantVisitor(String ngram, SuffixTree root) {
	this.ngram = ngram;
        this.root = root;

        stringWindow = new OuterStringWindow(ngram, ngram.length() - 1);
    }

    public SuffixTreeVisitor spawn(String ngram) {
        return new MarkRedundantVisitor(this.ngram + ngram, root);
    }

    public void visit(SuffixTree node) {
        for (String partial : stringWindow) {
            try {
                SuffixTree current = root.find(partial);
                if (!current.isRedundant() &&
                    current.isSubset(node, ngram.indexOf(partial))) {
                    current.markRedundant();
                }
            }
            catch (NoSuchElementException error) {}
        }
    }
}
