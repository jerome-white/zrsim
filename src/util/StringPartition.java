package util;

public class StringPartition {
    public final String head;
    public final String tail;
        
    public StringPartition(String string, int depth) {
        head = string.substring(0, depth);
        tail = (string.length() > depth) ?
            string.substring(1, string.length()) :
            new String();
    }

    public StringPartition(String string) {
        this(string, 1);
    }
}
