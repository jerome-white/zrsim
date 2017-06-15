package test.system;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

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

        SuffixTree root = new SuffixTree();

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

        /*
         * Prune the tree by marking entries that are redundant
         */
        LOGGER.info("Term selection");

        root.getChildren().forEach(1, (k, v) -> {
                SuffixTreeVisitor visitor =
                    new MarkRedundantVisitor(k.toString(), root, min_ngrams);
                v.accept(visitor);
            });

        /*
         * Dump the tree to disk
         */
        LOGGER.info("Terms to disk");

        try (PrintStream printStream =
             new PrintStream(new FileOutputStream(output))) {
            root.getChildren().forEach(1, (k, v) -> {
                    SuffixTreeVisitor visitor =
                        new OutputVisitor(k.toString(),
                                          min_ngrams,
                                          2,
                                          false,
                                          printStream);
                    v.accept(visitor);
                });
        }
        catch (FileNotFoundException error) {}

        LOGGER.info("Complete");
    }
}
