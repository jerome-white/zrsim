package test.system;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

import tree.SuffixTree;
import tree.DocumentParser;
import visitor.OutputVisitor;
import visitor.SuffixTreeVisitor;
import visitor.MarkRedundantVisitor;

public class Manager {
    private final static Logger LOGGER =
        Logger.getLogger(Manager.class.getName());

    public static void main(String[] args) {
        LOGGER.setLevel(Level.INFO);

        File directory = new File(args[0]);
        int min_ngrams = Integer.parseInt(args[1]);
        int max_ngrams = Integer.parseInt(args[2]);
        File output = new File(args[3]);
        File tmpdir = (args.length > 4) ? new File(args[4]) : null;

        SuffixTree root = new SuffixTree(min_ngrams);

        /*
         * add the documents to the suffix tree
         */
        LOGGER.info("Adding terms");

        int pool = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(pool);

        for (File document : directory.listFiles()) {
            assert !document.isDirectory();
            es.execute(new DocumentParser(root, document, max_ngrams));
        }
        es.shutdown();

        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }

        /*
         * Prune the tree by marking entries that are redundant
         */
        LOGGER.info("Term selection");

        root.getChildren().forEach(1, (k, v) -> {
                v.accept(new MarkRedundantVisitor(k, root));
            });

        /*
         * Dump the tree to disk
         */
        LOGGER.info("Terms to disk");

        ConcurrentHashMap<String, File> fragments =
            new ConcurrentHashMap<String, File>();
        root.getChildren().forEach(1, (k, v) -> {
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

        root.getChildren().forEach(1, (k, v) -> {
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

        LOGGER.info("Complete");
    }
}
