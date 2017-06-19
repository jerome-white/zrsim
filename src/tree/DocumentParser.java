package tree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.Runnable;

public class DocumentParser implements Runnable {
    private int window;
    private File file;
    private SuffixTree tree;
    
    public DocumentParser(SuffixTree tree, File file, int window) {
        this.file = file;
        this.window = window;
        this.tree = tree;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            int skip = 1;
            char[] buffer = new char[window];
            String document = file.getName();
            
            for (int i = 0; ; i += skip) {
                in.mark(window);
                int len = in.read(buffer, 0, window);
                if (len != window) {
                    break;
                }

                tree.add(new String(buffer), document, i);
                
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
