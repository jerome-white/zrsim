package simulate.task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import tree.SuffixTree;
import simulate.Manager;

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
	String document = path.getFileName().toString();
	Manager.LOGGER.info(document);

        try (FileChannel fc = FileChannel.open(path)) {
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
        catch (IOException ex) {
	    throw new UncheckedIOException(ex);
        }

	return document;
    }
}
