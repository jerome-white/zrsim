import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
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
        char[] buffer = new char[n];

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            for (int i = 0; ; i++) {
                in.mark(n + 1);
                len = in.read(buffer, 0, n);
                if (len != n) {
                    break;
                }

                tree.add(new String(buffer), new Location(file, i));
                
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
