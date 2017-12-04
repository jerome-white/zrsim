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
import util.NgramCollection;
import util.transform.NgramTransformer;
import util.transform.IdentityTransformer;

public class DocumentParser implements Callable<String> {
    private int min;
    private int max;

    private Path path;
    private NgramCollection collection;
    private NgramTransformer transformer;

    public DocumentParser(NgramCollection collection,
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

    public DocumentParser(NgramCollection collection,
                          Path path,
                          int min,
                          int max) {
        this(collection, path, min, max, new IdentityTransformer());
    }

    public DocumentParser(NgramCollection collection, Path path, int n) {
        this(collection, path, n, n);
    }

    public String call() {
        String document = path.getFileName().toString();
        LogAgent.LOGGER.info(document);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            StringBuffer buffer = new StringBuffer(max);

            for (int pos = 0; ; pos++) {
                /*
                 * mark the location
                 */
                reader.mark(max);

                /*
                 * read the ngram
                 */
                for (int i = 0; i < max; i++) {
                    int c = reader.read();
                    if (c < 0) {
                        break;
                    }
                    buffer.append((char)c);
                }
                if (buffer.length() < min) {
                    break;
                }
                String ngram = buffer.toString();
                buffer.delete(0, buffer.length());

                /*
                 * transform, advancing the location regardless
                 */
                try {
                    ngram = transformer.transform(ngram);
                }
                catch (IllegalArgumentException ex) {
                    continue;
                }
                finally {
                    reader.reset();
                    reader.skip(1);
                }

                /*
                 * add to the tree
                 */
                collection.add(ngram, document, pos);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
