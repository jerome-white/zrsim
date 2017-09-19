package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import util.LogAgent;
import index.SuffixTree;

public class DocumentParser implements Callable<String> {
    private int min;
    private int max;

    private Path path;
    private String encoding;
    private SuffixTree tree;

    public DocumentParser(SuffixTree tree, Path path, int min, int max) {
        this.tree = tree;
        this.path = path;
        this.min = min;
        this.max = max;

        encoding = System.getProperty("file.encoding");
    }

    public String call() {
        String document = path.getFileName().toString();
        LogAgent.LOGGER.info(document);

        try (FileChannel fc = FileChannel.open(path)) {
            byte[] bytes = new byte[max];
            ByteBuffer buffer = ByteBuffer.allocate(max);

            for (int i = 0; ; i++) {
                fc.read(buffer, i);

                int read = max - buffer.remaining();
                if (read < min) {
                    break;
                }

                buffer.rewind();
                buffer.get(bytes);
                buffer.clear();

                CharBuffer ngram = Charset
                    .forName(encoding)
                    .decode(ByteBuffer.wrap(bytes, 0, read));
                tree.add(ngram, document, i);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
