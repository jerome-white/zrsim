package visitor;

import java.util.NoSuchElementException;

import util.SuffixTree;
import util.window.StringWindow;
import util.window.ComprehensiveStringWindow;

public class MarkRedundantVisitor implements SuffixTreeVisitor {
    private String ngram;
    private SuffixTree root;
    private StringWindow stringWindow;

    public MarkRedundantVisitor(String ngram, SuffixTree root) {
        this.ngram = ngram;
        this.root = root;

        stringWindow = new ComprehensiveStringWindow(ngram);
    }

    public SuffixTreeVisitor spawn(String ngram) {
        return new MarkRedundantVisitor(this.ngram + ngram, root);
    }

    public void visit(SuffixTree node) {
        assert root.find(ngram) == node;

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
