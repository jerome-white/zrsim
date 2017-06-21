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
import java.lang.Long;
import java.lang.IllegalStateException;
import java.util.Map;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

import tree.SuffixTree;
import util.DocumentParser;
import visitor.OutputVisitor;
import visitor.MarkRedundantVisitor;

public class Manager {
    private final static Logger LOGGER =
        Logger.getLogger(Manager.class.getName());

    private SuffixTree suffixTree;

    public Manager(int min_ngram) {
        suffixTree = new SuffixTree(min_ngram);
    }

    public void addDocuments(Path corpus, int max_ngram) {
        LOGGER.info("Adding terms");

        DocumentParser parser = new DocumentParser(suffixTree, max_ngram);
        try {
            Files.list(corpus).parallel().forEach(p -> parser.parse(p));
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
        String slurm = "SLURM_JOBTMP";
        Map<String, String> env = System.getenv();
        Path tmpdir = env.containsKey(slurm) ?
            Paths.get(env.get(slurm)) : null;

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
             FileChannel.open(output, StandardOpenOption.WRITE)) {
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

    public static void main(String[] args) {
        LOGGER.setLevel(Level.INFO);

        Path directory = Paths.get(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        Path output = Paths.get(args[3]);

        LOGGER.info("Begin");

        Manager manager = new Manager(min_ngram);
        manager.addDocuments(directory, max_ngram);
        manager.selectTerms();
        manager.generate(output);

        LOGGER.info("Complete");
    }
}
