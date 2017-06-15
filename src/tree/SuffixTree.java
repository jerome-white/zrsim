package tree;

import java.lang.Character;
import java.lang.CharSequence;
import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

import util.Location;
import visitor.SuffixTreeVisitor;

public class SuffixTree {
    private AtomicBoolean redundant;
    private ConcurrentHashMap<Character, SuffixTree> children;
    private ConcurrentLinkedQueue<Location> locations;

    public SuffixTree() {
        redundant = new AtomicBoolean(false);
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

    public void markRedundant() {
        redundant.set(true);
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

    public void accept(SuffixTreeVisitor visitor) {
        children.forEach((k, v) -> v.accept(visitor.spawn(k)));
        visitor.visit(this);
    }
}
