package simulate;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UncheckedIOException;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.lang.Long;
import java.lang.IllegalStateException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

import tree.SuffixTree;
import tree.DocumentParser;
import visitor.OutputVisitor;
import visitor.MarkRedundantVisitor;

public class Manager {
    private final static Logger LOGGER =
        Logger.getLogger(Manager.class.getName());

    private SuffixTree suffixTree;

    public Manager(int min_ngram) {
        suffixTree = new SuffixTree(min_ngram);
    }

    public void addDocuments(File corpus, int max_ngram) {
        LOGGER.info("Adding terms");

        int pool = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(pool);

        for (File document : corpus.listFiles()) {
            assert !document.isDirectory();
            es.execute(new DocumentParser(suffixTree, document, max_ngram));
        }
        es.shutdown();

        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void selectTerms() {
        LOGGER.info("Term selection");

        suffixTree.getChildren().forEach(1, (k, v) -> {
                v.accept(new MarkRedundantVisitor(k, suffixTree));
            });
    }

    public void generate(File output) {
        String slurm = "SLURM_JOBTMP";
        Map<String, String> env = System.getenv();
        File tmpdir = env.containsKey(slurm) ? new File(env.get(slurm)) : null;

        ConcurrentHashMap<String, File> fragments =
            new ConcurrentHashMap<String, File>();
        suffixTree.getChildren().forEachKey(1, k -> {
                try {
                    File tmpfile = File.createTempFile(k, null, tmpdir);
                    tmpfile.deleteOnExit();
                    assert !fragments.containsKey(k);
                    File previous = fragments.put(k, tmpfile);
                }
                catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        LOGGER.info("Terms to disk");

        suffixTree.getChildren().forEach(1, (k, v) -> {
                File file = fragments.get(k);
                try (PrintStream out =
                     new PrintStream(new FileOutputStream(file), true)) {
                    v.accept(new OutputVisitor(k, 2, false, out));
                }
                catch (FileNotFoundException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        LOGGER.info("Disk consolidation");

        try (FileChannel dest = new FileOutputStream(output).getChannel()) {
            for (File in : fragments.values()) {
                try (FileChannel src = new FileInputStream(in).getChannel()) {
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

        File directory = new File(args[0]);
        int min_ngram = Integer.parseInt(args[1]);
        int max_ngram = Integer.parseInt(args[2]);
        File output = new File(args[3]);

        LOGGER.info("Begin");

        Manager manager = new Manager(min_ngram);
        manager.addDocuments(directory, max_ngram);
        manager.selectTerms();
        manager.generate(output);

        LOGGER.info("Complete");
    }
}
