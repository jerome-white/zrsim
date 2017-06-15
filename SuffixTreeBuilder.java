import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.nio.CharBuffer;
import java.lang.Runnable;

public class SuffixTreeBuilder implements Runnable {
    private int n;
    private File file;
    private SuffixTree tree;

    public SuffixTreeBuilder(SuffixTree tree, File file, int n) {
        this.file = file;
        this.n = n;
        this.tree = tree;
    }

    public void run() {
        int len;
        int skip = 1;
        CharBuffer buffer = CharBuffer.allocate(n);
        String document = file.getName();

        try (Reader in = new BufferedReader(new FileReader(file))) {
            for (int i = 0; ; i += skip) {
                in.mark(n);
                len = in.read(buffer);
                if (len != n) {
                    break;
                }
                buffer.flip();

                tree.add(buffer, new Location(document, i));

                buffer.clear();
                in.reset();
                in.skip(skip);
            }
            in.close();
        }
        catch (IOException error) {
            System.err.println(error);
        }
    }
}
