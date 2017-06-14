import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SuffixTree {
    private final int key_length = 1;
    
    private AtomicBoolean redundant;
    private ConcurrentHashMap<String, SuffixTree> children;
    private ConcurrentLinkedQueue<Location> locations;

    public SuffixTree() {
        this.redundant = new AtomicBoolean(false);
        children = new ConcurrentHashMap<String, SuffixTree>();
        locations = new ConcurrentLinkedQueue<Location>();
    }

    public ConcurrentHashMap<String, SuffixTree> getChildren() {
        return children;
    }

    public ConcurrentLinkedQueue<Location> getLocations() {
        return locations;
    }
    
    public boolean isRedundant() {
        return redundant.get();
    }

    public void add(String ngram, Location location) {
        String head = ngram.substring(0, key_length);
        children.putIfAbsent(head, new SuffixTree());
        
        SuffixTree child = children.get(head);
        child.locations.add(location);

        if (ngram.length() > key_length) {
            String tail = ngram.substring(key_length);
            child.add(tail, location);
        }
    }
    
    public SuffixTree find(String ngram) {
        if (ngram.isEmpty()) {
            throw new java.lang.IllegalArgumentException();
        }
        
        String head = ngram.substring(0, key_length);
        SuffixTree child = children.get(head);
        if (child == null) {
            throw new NoSuchElementException(head);
	}

        return (ngram.length() == key_length) ?
            child : child.find(ngram.substring(key_length));
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

    // private void dump(String indent) {
    //     for (Location l : locations) {
    //         System.out.print("(" + l.document + " " + l.offset + ") ");
    //     }
    //     System.out.println();

    //     children.forEach((k, v) -> {
    //             System.out.print(indent + k + " -> ");
    //             v.dump(indent + " ");
    //         });
    // }

    // public void dump() {
    //     dump(new String());
    // }
}
