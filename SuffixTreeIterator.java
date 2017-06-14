import java.lang.Iterable;
import java.util.Deque;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.Collections;

public class SuffixTreeIterator implements Iterator<Token>, Iterable<Token> {
    private int appearances;
    private int length;
    private boolean redundants;
    private String ngram;
    private SuffixTree root;
    private Iterator<Location> locations;
    private Deque<String> children;
    
    public SuffixTreeIterator(SuffixTree root,
                              int length,
                              int appearances,                              
                              boolean redundants,
                              String ngram) {
        this.root = root;
        this.length = length;
        this.appearances = appearances;
        this.redundants = redundants;
        this.ngram = ngram;

        /*
         * 1. Is the length of the n-gram long enough?
         * 2. Does the term appear in enough places?
         * 3. Has the term been marked redundant (if concerned)?
         */
        if (ngram.length() < length ||
            root.getLocations().size() < appearances ||
            !redundants && root.isRedundant()) {
            locations = Collections.emptyIterator();
        }
        else {
            locations = root.getLocations().iterator();
        }
        
        children = new ArrayDeque<String>(root.getChildren().keySet());
    }

    public SuffixTreeIterator(SuffixTree root,
                              int length,
                              int appearances,
                              boolean redundants) {
        this(root, length, appearances, redundants, new String());
    }
    
    public SuffixTreeIterator(SuffixTree root, int length, int appearances) {
        this(root, length, appearances, true);
    }
    
    public SuffixTreeIterator(SuffixTree root, int length) {
        this(root, length, 0);
    }

    public SuffixTreeIterator(SuffixTree root) {
        this(root, 0);
    }

    public Iterator<Token> iterator() {
        return this;
    }

    public boolean hasNext() {
        return locations.hasNext() || !children.isEmpty();
    }

    public Token next() {
        if (locations.hasNext()) {
            return new Token(ngram, locations.next());
        }

        String key = children.pop();
        SuffixTree child = root.getChildren().get(key);
        SuffixTreeIterator it = new SuffixTreeIterator(child,
                                                       length,
                                                       appearances,
                                                       redundants,
                                                       ngram + key);
        if (it.hasNext()) {
            return it.next();
        }

        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
