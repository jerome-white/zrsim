package util;

import java.lang.Iterable;
import java.util.Iterator;

public class StringWindow implements Iterator<String>, Iterable<String> {
    private String string;
    private int chunk;
    private int cursor;

    public StringWindow(String string, int chunk) {
        this.string = string;
        this.chunk = chunk;
        cursor = 0;
    }

    public Iterator<String> iterator() {
        return this;
    }

    public boolean hasNext() {
        return chunk > 0 && cursor + chunk <= string.length();
    }

    public String next() {
        int start = cursor;
        cursor++;

        return string.substring(start, start + chunk);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
