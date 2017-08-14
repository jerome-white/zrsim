package exec.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import util.Term;
import util.Token;
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

        try (PrintStream printStream =
             new PrintStream(Files.newOutputStream(output), true)) {
            index.forEachToken(document, t -> {
                    String name = termNamer.get(t.getNgram());
                    Term term = new Term(t, name);
                    printStream.println(term);
                });
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
