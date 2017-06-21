package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Files;

import tree.SuffixTree;

public class DocumentParser {
    private int window;
    private SuffixTree tree;

    public DocumentParser(SuffixTree tree, int window) {
        this.tree = tree;
        this.window = window;
    }

    private BufferedReader pathToReader(Path path) throws IOException {
        InputStream in = Files.newInputStream(path);
        return new BufferedReader(new InputStreamReader(in));
    }

    public void parse(Path path) {
        try (BufferedReader reader = pathToReader(path)) {
            int len = 0;
            int skip = 1;
            char[] buffer = new char[window];
            String document = path.getFileName().toString();

            for (int i = 0; ; i += skip) {
                reader.mark(window);
                len = reader.read(buffer, 0, window);
                if (len != window) {
                    break;
                }

                tree.add(String.valueOf(buffer), document, i);

                reader.reset();
                reader.skip(skip);
            }
        }
        catch (IOException error) {
            System.err.println(error);
        }
    }
}
