package util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import tree.SuffixTree;

public class DocumentParser implements Callable<String> {
    private int window;

    private SuffixTree tree;
    private Path path;
    private Charset charset;

    public DocumentParser(SuffixTree tree, int window, Path path) {
        this.tree = tree;
        this.window = window;
	this.path = path;

	charset = Charset.forName("UTF-8");
    }

    public String call() {
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

	    return document;
        }
        catch (IOException error) {
            System.err.println(error);
        }

	return null;
    }
}
