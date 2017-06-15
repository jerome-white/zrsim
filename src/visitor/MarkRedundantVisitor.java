package visitor;

import java.util.NoSuchElementException;

import tree.SuffixTree;
import util.Location;
import util.StringWindow;

public class MarkRedundantVisitor implements SuffixTreeVisitor {
    private String ngram;
    private SuffixTree root;
    private StringWindow stringWindow;

    public MarkRedundantVisitor(String ngram, SuffixTree root) {
	this.ngram = ngram;
        this.root = root;

        stringWindow = new StringWindow(ngram, ngram.length() - 1);
    }

    public SuffixTreeVisitor spawn(Character gram) {
        return new MarkRedundantVisitor(ngram + gram, root);
    }

    public void visit(SuffixTree node) {
        for (String partial : stringWindow) {
            try {
                SuffixTree current = root.find(partial);
                if (current.isRedundant()) {
                    continue;
                }

                int overlap = 0;
                int epsilon = ngram.length() - partial.length();

                for (Location theirs : current.getLocations()) {
                    for (Location ours : node.getLocations()) {
                        if (ours.contains(theirs, epsilon)) {
                            overlap++;
                            break;
                        }
                    }
                }

                if (overlap == current.getLocations().size()) {
                    current.markRedundant();
                }
            }
            catch (NoSuchElementException error) {}
            catch (IllegalArgumentException error) {}
        }
    }
}
