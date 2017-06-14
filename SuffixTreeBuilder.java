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
        CharBuffer buffer = CharBuffer.allocate(n);

        try (Reader in = new BufferedReader(new FileReader(file))) {
            for (int i = 0; ; i++) {
                in.mark(n + 1);
                len = in.read(buffer);
                if (len != n) {
                    break;
                }
                buffer.flip();
                
                tree.add(buffer, new Location(file, i));
                
                buffer.clear();
                in.reset();
                in.skip(1);
            }
            in.close();
        }
        catch (IOException error) {
            System.err.println(error);
        }
    }
}
