import java.lang.Comparable;

public class Location implements Comparable<Location> {
    public final int offset;

    public final String document;

    public Location(String document, int offset) {
        this.document = document;
        this.offset = offset;
    }

    public int compareTo(Location o) {
        int cmp = document.compareTo(o.document);

        return (cmp == 0) ? Integer.compare(offset, o.offset) : cmp;
    }

    public boolean contains(Location o, int epsilon) {
        return document.equals(o.document) &&
            (o.offset == offset || o.offset <= offset + epsilon);
    }
}
