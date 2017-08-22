package util.window;

import java.lang.Iterable;
import java.util.Iterator;

public abstract class StringWindow implements Iterator<String>,
                                              Iterable<String> {
    public abstract boolean hasNext();
    public abstract String next();

    public Iterator<String> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
