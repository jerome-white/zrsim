package util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;

import tree.SuffixTree;

public class DocumentParser {
    private int window;
    private SuffixTree tree;
    private Charset charset;

    public DocumentParser(SuffixTree tree, int window) {
        this.tree = tree;
        this.window = window;
	charset = Charset.forName("UTF-8");
    }

    public void parse(Path path) {
        try (FileChannel fc = FileChannel.open(path)) {
            String document = path.getFileName().toString();
            ByteBuffer buffer = ByteBuffer.allocate(window);

            for (int i = 0; ; i++) {
                fc.read(buffer, i);
                if (buffer.hasRemaining()) {
                    break;
                }
                buffer.flip();
                tree.add(charset.decode(buffer), document, i);
                buffer.rewind();
            }
        }
        catch (IOException error) {
            System.err.println(error);
        }
    }
}
