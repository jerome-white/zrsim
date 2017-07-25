package simulate;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UncheckedIOException;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;

import util.SuffixTree;
import visitor.OutputVisitor;
import visitor.MarkRedundantVisitor;
import simulate.task.DocumentParser;

public class StreamManager extends Manager {
    public StreamManager(int min_gram) {
        super(min_gram);
    }

    public void addDocuments(Path corpus, int max_ngram) {
        LOGGER.info("Adding terms");


        try {
            Files
                .list(corpus)
                .parallel()
                .forEach(p -> (new DocumentParser(suffixTree, max_ngram, p))
                         .call());
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void selectTerms() {
        LOGGER.info("Term selection");

        suffixTree.getChildren().forEach(1, (k, v) -> {
                v.accept(new MarkRedundantVisitor(k, suffixTree));
            });
    }

    public void generate(Path output) {
        Map<String, String> env = System.getenv();
        Path tmpdir = env.containsKey(SLURM_JOBTMP) ?
            Paths.get(env.get(SLURM_JOBTMP)) : null;

        ConcurrentHashMap<String, Path> fragments =
            new ConcurrentHashMap<String, Path>();

        suffixTree.getChildren().forEachKey(1, k -> {
                assert !fragments.containsKey(k);
                try {
                    Path tmpfile = (tmpdir == null) ?
                        Files.createTempFile(k, null) :
                        Files.createTempFile(tmpdir, k, null);
                    fragments.put(k, tmpfile);
                }
                catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        LOGGER.info("Terms to disk");

        suffixTree.getChildren().forEach(1, (k, v) -> {
                Path path = fragments.get(k);
                try (PrintStream printStream =
                     new PrintStream(Files.newOutputStream(path), true)) {
                    v.accept(new OutputVisitor(k, 2, false, printStream));
                }
                catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        LOGGER.info("Disk consolidation");

        try (FileChannel dest =
             FileChannel.open(output,
                              StandardOpenOption.WRITE,
                              StandardOpenOption.CREATE,
                              StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path input : fragments.values()) {
                try (FileChannel src =
                     FileChannel.open(input,
                                      StandardOpenOption.DELETE_ON_CLOSE)) {
                    dest.transferFrom(src, dest.size(), src.size());
                }
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
