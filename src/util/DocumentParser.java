package util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;

import tree.SuffixTree;

public class DocumentParser {
    private final Charset decoder = StandardCharsets.UTF_8;

    private int window;
    private SuffixTree tree;

    public DocumentParser(SuffixTree tree, int window) {
        this.tree = tree;
        this.window = window;
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
                tree.add(decoder.decode(buffer).toString(), document, i);
                buffer.rewind();
            }
        }
        catch (IOException error) {
            System.err.println(error);
        }
    }
}
