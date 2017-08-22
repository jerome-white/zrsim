package util.window;

public class OuterStringWindow extends StringWindow {
    private String string;
    private int chunk;
    private int cursor;

    public OuterStringWindow(String string, int chunk) {
        this.string = string;
        this.chunk = chunk;
        cursor = 0;
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
