package tree;

import java.lang.Long;
import java.lang.IllegalStateException;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import util.StringPartition;
import visitor.SuffixTreeVisitor;

public class SuffixTree {
    private final int key_length;

    private AtomicBoolean redundant;
    private ConcurrentHashMap<String, SuffixTree> children;
    private ConcurrentHashMap<String, SortedSet<Integer>> locations;

    public SuffixTree(int key_length) {
        this.key_length = key_length;

        redundant = new AtomicBoolean(false);
        children = new ConcurrentHashMap<String, SuffixTree>();
        locations = new ConcurrentHashMap<String, SortedSet<Integer>>();
    }

    public SuffixTree() {
        this(1);
    }

    public ConcurrentHashMap<String, SuffixTree> getChildren() {
        return children;
    }

    public ConcurrentHashMap<String, SortedSet<Integer>> getLocations() {
        return locations;
    }

    public boolean isRedundant() {
        return redundant.get();
    }

    public void markRedundant() {
        redundant.set(true);
    }

    public void accept(SuffixTreeVisitor visitor) {
        children.forEach((k, v) -> v.accept(visitor.spawn(k)));
        visitor.visit(this);
    }

    public void add(String ngram, String document, int offset) {
        if (!ngram.isEmpty()) {
            StringPartition partition = new StringPartition(ngram, key_length);
            SuffixTree child = children.computeIfAbsent(partition.head,
                                                        k -> new SuffixTree());
            child.locations.compute(document, (k, v) -> {
                    if (v == null) {
                        v = new TreeSet<Integer>();
                    }
                    v.add(offset);
                    return v;
                });
            child.add(partition.tail, document, offset);
        }
    }

    public SuffixTree find(String ngram) {
        if (ngram.isEmpty()) {
            return this;
        }

        if (ngram.length() >= key_length) {
            StringPartition partition = new StringPartition(ngram, key_length);
            SuffixTree child = children.get(partition.head);
            if (child != null) {
                return child.find(partition.tail);
            }
        }

        throw new NoSuchElementException();
    }

    public int appearances() {
        int hits = 0;

        for (String document : locations.keySet()) {
            hits += locations.get(document).size();
        }

        return hits;
    }

    /*
     * It is assumed that this will be called for ngrams that are
     * string-subsets of one another.
     */
    public boolean isSubset(SuffixTree node, int epsilon) {
        assert epsilon >= 0;

        /*
         * A node with an empty location index is invalid in
         * principle; however, without this check the method would
         * return 'true', which would be difficult to interpret.
         */
        if (locations.isEmpty()) {
            throw new IllegalStateException();
        }

        for (String document : locations.keySet()) {
            /*
             * Should appear in more documents.
             */
            SortedSet<Integer> theirOffsets = node.locations.get(document);
            if (theirOffsets == null) {
                return false;
            }

            /*
             * An appearance is a subset if the offset is either
             * exactly aligned or "within" the other.
             */
            for (Integer myOffset : locations.get(document)) {
                if (!theirOffsets.contains(myOffset) &&
                    !theirOffsets.contains(myOffset - epsilon)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isSubset(SuffixTree node) {
        return isSubset(node, 0);
    }
}
