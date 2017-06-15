import java.io.File;
import java.lang.Comparable;

public class Location implements Comparable<Location> {
    public final File document;
    public final int offset;

    public Location(File document, int offset) {
        this.document = document;
        this.offset = offset;
    }

    public Location(String document, int offset) {
        this(new File(document), offset);
    }

    public int compareTo(Location o) {
        int cmp = document.getName().compareTo(o.document.getName());

        return (cmp == 0) ? Integer.compare(offset, o.offset) : cmp;
    }

    public boolean contains(Location o, int epsilon) {
        return document.equals(o.document) &&
            (o.offset == offset || o.offset <= offset + epsilon);
    }
}
