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

        /*
         * Prune the tree by marking entries that are redundant
         */
        LOGGER.info("Term selection");

        root.getChildren().forEach(1, (k, v) -> {
                v.accept(new MarkRedundantVisitor(k.toString(), root));
            });

        /*
         * Dump the tree to disk
         */
        LOGGER.info("Terms to disk");

        try (PrintStream out = new PrintStream(new FileOutputStream(output))) {
            root.getChildren().forEach(1, (k, v) -> {
                    v.accept(new OutputVisitor(k.toString(), 2, false, out));
                });
        }
        catch (FileNotFoundException error) {}

        LOGGER.info("Complete");
    }
}
