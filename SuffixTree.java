import java.lang.Character;
import java.lang.CharSequence;
import java.lang.IllegalArgumentException;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;
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

        StringWindow window = new StringWindow(ngram, ngram.length() - 1);
        for (String n : window) {
            try {
                SuffixTree node = root.find(n);
                if (node.isRedundant()) {
                    continue;
                }

                int epsilon = ngram.length() - n.length();

                SortedSet<Location> theirLocationSet =
                    new TreeSet<Location>(node.locations);
                Iterator<Location> theirSet = theirLocationSet.iterator();

                SortedSet<Location> myLocationSet =
                    new TreeSet<Location>(locations);
                Iterator<Location> ourSet = myLocationSet.iterator();

                while (theirSet.hasNext()) {
                    Location theirLocation = theirSet.next();
                    while (ourSet.hasNext()) {
                        Location ourLocation = ourSet.next();
                        if (ourLocation.contains(theirLocation, epsilon)) {
                            theirSet.remove();
                            break;
                        }
                    }
                }

                if (theirLocationSet.isEmpty()) {
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
