package exec.task;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

import util.LogAgent;
import util.namer.TermNamer;
import util.entity.Term;
import util.entity.Token;
import index.ForwardIndex;

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

        StringJoiner terms = new StringJoiner("\n", "", "\n");
        terms.setEmptyValue(""); // if nothing's been added, length should be 0

        index.forEachToken(document, t -> {
                String name = termNamer.get(t.getNgram());
                Term term = new Term(t, name);
                if (terms.length() == 0) {
                    terms.add(term.getFields());
                }
                terms.add(term.toString());
            });

        Path output = root.resolve(document);
        try (OutputStream out = Files.newOutputStream(output)) {
            out.write(terms.toString().getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
