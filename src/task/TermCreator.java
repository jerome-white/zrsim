package task;

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
import util.entity.Posting;
import index.ForwardIndex;

public class TermCreator implements Callable<String> {
    private class TermJoiner {
        private StringJoiner string;

        public TermJoiner() {
            string = new StringJoiner("\n", "", "\n");
            string.setEmptyValue("");
        }

        public boolean isEmpty() {
            return string.length() == 0;
        }

        public void push(Term term) {
            if (isEmpty()) {
                string.add(term.getFields());
            }
            string.add(term.toString());
        }

        public byte[] toBytes() {
            return string.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

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

        TermJoiner termJoiner = new TermJoiner();
        index.forEachPosting(document, p -> {
                String name = termNamer.get(p.getNgram());
                termJoiner.push(new Term(name, p));
            });

        Path output = root.resolve(document);
        try (OutputStream out = Files.newOutputStream(output)) {
            out.write(termJoiner.toBytes());
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
