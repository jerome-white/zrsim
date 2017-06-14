import java.io.File;
import java.lang.Comparable;

public class Location {
    public final File document;
    public final int offset;

    public Location(File document, int offset) {
        this.document = document;
        this.offset = offset;
    }

    public Location(String document, int offset) {
        this(new File(document), offset);
    }
}
