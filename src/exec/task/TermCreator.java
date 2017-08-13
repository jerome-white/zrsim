package exec.task;

import java.io.PrintStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import util.Term;
import util.Token;
import util.LogAgent;
import util.TermNamer;
import util.ForwardIndex;

public class TermCreator implements Callable<String> {
    private Path root;
    private ForwardIndex index;
    private Collection<String> documents;
    private TermNamer termNamer;

    public TermCreator(ForwardIndex index,
                       Collection<String> documents,
                       TermNamer termNamer,
                       Path root) {
        this.index = index;
        this.documents = documents;
        this.termNamer = termNamer;
        this.root = root;
    }

    public String call() {
        StringJoiner success = new StringJoiner(",");

        for (String document : documents) {
            LogAgent.LOGGER.info(document);

            Path output = root.resolve(document);
            try (PrintStream printStream =
                 new PrintStream(Files.newOutputStream(output), true)) {
                index.forEachToken(document, t -> {
                        String name = termNamer.get(t.getNgram());
                        Term term = new Term(t, name);
                        printStream.println(term);
                    });
                success.add(document);
            }
            catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return success.toString();
    }
}
