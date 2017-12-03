package task;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import util.LogAgent;
import util.NGramCollection;
import util.keeper.GateKeeper;
import util.transform.NgramTransformer;
import util.transform.IdentityTransformer;

public class DocumentParser implements Callable<String> {
    private int min;
    private int max;

    private Path path;
    private NGramCollection collection;
    private NgramTransformer transformer;

    public DocumentParser(NGramCollection collection,
                          Path path,
                          int min,
                          int max,
                          NgramTransformer transformer) {
        this.collection = collection;
        this.path = path;
        this.min = min;
        this.max = max;
        this.transformer = transformer;
    }

    public DocumentParser(NGramCollection collection,
                          Path path,
                          int min,
                          int max) {
        this(collection, path, min, max, new IdentityTransformer());
    }

    public DocumentParser(NGramCollection collection, Path path, int n) {
        this(collection, path, n, n);
    }

    public String call() {
        String document = path.getFileName().toString();
        LogAgent.LOGGER.info(document);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            int position = 0;
            char[] buffer = new char[max];

            while (true) {
                reader.mark(max);

                int read = reader.read(buffer, 0, max);
                if (read < min) {
                    break;
                }

                String ngram = new String(buffer);
                try {
                    ngram = transformer.transform(ngram);
                    collection.add(ngram, document, position);
                }
                catch (IllegalArgumentException ex) {}

                reader.reset();
                reader.skip(1);
                position++;
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
