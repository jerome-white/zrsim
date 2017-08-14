package exec.task;

import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import util.Term;
import util.Token;
import util.LogAgent;
import util.ForwardIndex;

public class TokenCollector implements Callable<ForwardIndex> {
    private int focus;
    private int block;

    private Path path;

    public TokenCollector(Path path, int focus, int block) {
        this.path = path;
        this.focus = focus;
        this.block = block;
    }

    public ForwardIndex call() {
        try (InputStream in = Files.newInputStream(path);
             InputStreamReader stream = new InputStreamReader(in);
             LineNumberReader reader = new LineNumberReader(stream)) {
            ForwardIndex index = new ForwardIndex();
            String order = String.valueOf(focus);

            LogAgent.LOGGER.info(order);

            for (int i = focus; ; ) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (reader.getLineNumber() - 1 == i) {
                    index.add(Token.fromString(line));
                    i += block;
                }
            }

            return index;
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
