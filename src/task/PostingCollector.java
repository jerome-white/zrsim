package task;

import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import util.LogAgent;
import util.entity.Posting;
import index.ForwardIndex;

public class PostingCollector implements Callable<ForwardIndex> {
    private int focus;
    private int block;

    private Path path;

    public PostingCollector(Path path, int focus, int block) {
        this.path = path;
        this.focus = focus;
        this.block = block;
    }

    public ForwardIndex call() {
        try (InputStream in = Files.newInputStream(path);
             InputStreamReader stream = new InputStreamReader(in);
             LineNumberReader reader = new LineNumberReader(stream)) {
            LogAgent.LOGGER.info(String.valueOf(focus));

            int interest = focus;
            ForwardIndex index = new ForwardIndex();

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (reader.getLineNumber() - 1 == interest) {
                    Posting posting = new Posting(line);
                    index.add(posting);
                    interest += block;
                }
            }

            return index;
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
