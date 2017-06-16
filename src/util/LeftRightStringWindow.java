package util;

import java.util.Iterator;

public class LeftRightStringWindow extends StringWindow {
    private String string;
    private int chunk;
    private int cursor;

    public LeftRightStringWindow(String string, int chunk) {
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
}
