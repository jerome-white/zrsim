package exec.task;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import util.Term;
import util.LogAgent;
import util.TermNamer;
import util.ForwardIndex;

public class TermCreator implements Callable<String> {
    private Path root;
    private String document;
    private TermNamer termNamer;
    private ForwardIndex index;

    public TermCreator(ForwardIndex index,
                       String document,
                       TermNamer termNamer,
                       Path root) {
        this.index = index;
        this.document = document;
        this.termNamer = termNamer;
        this.root = root;
    }

    public String call() {
        LogAgent.LOGGER.info(document);

        Path output = root.resolve(document);

        try (OutputStream out = Files.newOutputStream(output)) {
            index.forEachToken(document, t -> {
                    Term term = new Term(t, termNamer.get(t.getNgram()));
                    String line = term.toString() + "\n";
                    out.write(line.getBytes(StandardCharsets.UTF_8));
                });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
