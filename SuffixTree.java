import java.lang.Character;
import java.lang.CharSequence;
import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SuffixTree {
    private AtomicBoolean redundant;
    private ConcurrentHashMap<Character, SuffixTree> children;
    private ConcurrentLinkedQueue<Location> locations;

    public SuffixTree() {
        this.redundant = new AtomicBoolean(false);
        children = new ConcurrentHashMap<Character, SuffixTree>();
        locations = new ConcurrentLinkedQueue<Location>();
    }

    public ConcurrentHashMap<Character, SuffixTree> getChildren() {
        return children;
    }

    public ConcurrentLinkedQueue<Location> getLocations() {
        return locations;
    }

    public boolean isRedundant() {
        return redundant.get();
    }

    public void add(CharSequence ngram, Location location) {
        Character head = ngram.charAt(0);
        children.putIfAbsent(head, new SuffixTree());

        SuffixTree child = children.get(head);
        child.locations.add(location);

        if (ngram.length() > 1) {
            CharSequence tail = ngram.subSequence(1, ngram.length());
            child.add(tail, location);
        }
    }

    public SuffixTree find(String ngram) {
        if (ngram.isEmpty()) {
            throw new java.lang.IllegalArgumentException();
        }

        Character head = ngram.charAt(0);
        SuffixTree child = children.get(head);
        if (child == null) {
            throw new NoSuchElementException(head.toString());
	}

        return (ngram.length() > 1) ? child.find(ngram.substring(1)) : child;
    }

    private void markRedundants(SuffixTree root, String ngram) {
        children.forEach((k, v) -> v.markRedundants(root, ngram + k));

        int diff = 1;
        StringWindow window = new StringWindow(ngram, ngram.length() - diff);
        for (String n : window) {
            int overlap = 0;

            try {
                SuffixTree node = root.find(n);

                for (Location theirs : node.locations) {
                    for (Location ours : locations) {
                        if (theirs.document.equals(ours.document) &&
                            (theirs.offset == ours.offset ||
                             theirs.offset == ours.offset + diff)) {
                            overlap++;
                            break;
                        }
                    }
                }

                if (overlap == node.locations.size()) {
                    node.redundant.set(true);
                }
            }
            catch (NoSuchElementException error) {}
            catch (IllegalArgumentException error) {}
        }
    }

    public void markRedundants() {
        markRedundants(this, new String());
    }

    public void accept(SuffixTreeVisitor visitor) {
        visitor.visit(this);
        children.forEach((k, v) -> v.accept(visitor.spawn(k)));
    }
}
