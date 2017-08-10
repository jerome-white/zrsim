package exec.task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import exec.Simulator;
import util.SuffixTree;

public class DocumentParser implements Callable<String> {
    private int window;

    private String encoding;
    private Path path;
    private SuffixTree tree;

    public DocumentParser(SuffixTree tree, int window, Path path) {
        this.tree = tree;
        this.window = window;
        this.path = path;

        encoding = System.getProperty("file.encoding");
    }

    public String call() {
        String document = path.getFileName().toString();
        Simulator.LOGGER.info(document);

        try (FileChannel fc = FileChannel.open(path)) {
            ByteBuffer buffer = ByteBuffer.allocate(window);

            for (int i = 0; ; i++) {
                fc.read(buffer, i);
                if (buffer.hasRemaining()) {
                    break;
                }
                buffer.rewind();

                CharBuffer ngram = Charset.forName(encoding).decode(buffer);
                tree.add(ngram, document, i);

                buffer.clear();
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
