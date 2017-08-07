package generate.task;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import util.Term;
import util.Token;
import util.ForwardIndex;

public class TokenCollector implements Callable<String> {
    private int order;

    private ForwardIndex index;
    private Path path;

    public TokenCollector(ForwardIndex index, Path path, int order) {
        this.index = index;
        this.path = path;
        this.order = order;
    }

    public String call() {
        try (InputStream in = Files.newInputStream(path);
             InputStreamReader stream = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(stream)) {
            for (int i = 0; ; i++) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                if (i % order == 0) {
                    index.add(Token.fromString(line));
                }
            }

            return String.valueOf(order);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
