package tree;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.Location;
import util.StringPartition;
import visitor.SuffixTreeVisitor;

public class SuffixTree {
    private final int key_length;

    private AtomicBoolean redundant;
    private ConcurrentHashMap<String, SuffixTree> children;
    private ConcurrentLinkedQueue<Location> locations;

    public SuffixTree(int key_length) {
        this.key_length = key_length;

        redundant = new AtomicBoolean(false);
        children = new ConcurrentHashMap<String, SuffixTree>();
        locations = new ConcurrentLinkedQueue<Location>();
    }

    public SuffixTree() {
        this(1);
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

    public void markRedundant() {
        redundant.set(true);
    }

    public void add(String ngram, Location location) {
        if (ngram.isEmpty()) {
            locations.add(location);
        }
        else {
            StringPartition partition = new StringPartition(ngram, key_length);
            children.putIfAbsent(partition.head, new SuffixTree());
            SuffixTree child = children.get(partition.head);
            child.add(partition.tail, location);
        }
    }

    public SuffixTree find(String ngram) {
        if (ngram.isEmpty()) {
            return this;
        }

        StringPartition partition = new StringPartition(ngram, key_length);
        SuffixTree child = children.get(partition.head);
        if (child == null) {
            throw new NoSuchElementException();
	}

        return child.find(partition.tail);
    }

    public void accept(SuffixTreeVisitor visitor) {
        children.forEach((k, v) -> v.accept(visitor.spawn(k)));
        visitor.visit(this);
    }
}
