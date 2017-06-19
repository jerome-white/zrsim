package util;

public class StringPartition {
    public final String head;
    public final String tail;
        
    public StringPartition(String string, int pivot) {
        head = string.substring(0, pivot);
        tail = (pivot > string.length()) ?
            new String() : string.substring(pivot);
    }

    public StringPartition(String string) {
        this(string, 1);
    }
}
