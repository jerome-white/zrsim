package util;

import java.lang.Iterable;
import java.util.Iterator;

public abstract class StringWindow implements Iterator<String>,
                                              Iterable<String> {
    public abstract Iterator<String> iterator();
    public abstract boolean hasNext();
    public abstract String next();

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
